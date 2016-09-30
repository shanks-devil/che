/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc controller
 * @name workspaces.create.edit.workspace.controller:CreateEditWorkspaceController
 * @description This class is handling the controller for workspace creation and edition
 * @author Oleksii Kurinnyi
 */

const MIN_WORKSPACE_RAM: number = Math.pow(1024,3);
const DEFAULT_WORKSPACE_RAM: number = 2 * Math.pow(1024,3);

export class CreateEditWorkspaceController {
  $location;
  $log;
  $mdDialog;
  $q;
  $route;
  $rootScope: angular.IRootScopeService;
  $scope: angular.IScope;
  cheEnvironmentRegistry;
  cheNotification;
  cheWorkspace;
  ideSvc;
  workspaceDetailsService;

  loading: boolean = false;
  isCreateMode: boolean = true;
  selectedTabIndex: number; // TODO

  namespace: string;
  workspaceId: string;
  workspaceName: string;
  newName: string;
  stack: any;
  workspaceDetails: any = {};
  copyWorkspaceDetails: any = {};
  machinesViewStatus: any = {};
  errorMessage: string;
  invalidWorkspace: string;
  editMode: boolean = false;
  showApplyMessage: boolean = false;

  usedNamesList: any = [];// Array<string>

  forms: Map<string, any> = new Map();
  currentTab: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location, $log, $mdDialog, $q, $route, $rootScope, $scope, cheEnvironmentRegistry, cheNotification, cheWorkspace, ideSvc, workspaceDetailsService) {
    this.$log = $log;
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$route = $route;
    this.$rootScope = $rootScope;
    this.$scope = $scope;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.ideSvc = ideSvc;
    this.workspaceDetailsService = workspaceDetailsService;

    cheWorkspace.fetchWorkspaces().then(() => {
      let workspaces = cheWorkspace.getWorkspaces();
      workspaces.forEach((workspace) => {
        this.usedNamesList.push(workspace.config.name);
      });
    });

    $rootScope.showIDE = false;

    this.init();
  }

  init() {
    let routeParams = this.$route.current.params;
    if (routeParams && routeParams.namespace && routeParams.workspaceName) {
      this.isCreateMode = false;
      this.namespace = routeParams.namespace;
      this.workspaceName = routeParams.workspaceName;

      this.fetchWorkspaceDetails().then(() => {
        this.workspaceDetails = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
        this.updateWorkspaceData();
      });
    } else {
      this.isCreateMode = true;
      this.namespace = '';
      this.workspaceName = this.generateWorkspaceName();
    }
    this.newName = this.workspaceName;
  }

  /**
   * Fetches and gets workspace.
   *
   * @returns {*|IPromise<TResult>|Promise.<TResult>}
   */
  fetchWorkspaceDetails() {
    let defer = this.$q.defer();

    if (this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName)) {
      defer.resolve();
    } else {
      this.cheWorkspace.fetchWorkspaceDetails(this.namespace + ':' + this.workspaceName).then(() => {
        defer.resolve();
      }, (error) => {
        if (error.status === 304) {
          defer.resolve();
        } else {
          this.invalidWorkspace = error.statusText;
        }
      });
    }
    return defer.promise;
  }

  /**
   * Creates copy of workspace config.
   */
  updateWorkspaceData() {
    if (this.loading) {
      this.loading = false;
    }

    angular.copy(this.workspaceDetails, this.copyWorkspaceDetails);

    this.workspaceId = this.workspaceDetails.id;
    this.newName = this.workspaceDetails.config.name;
  }

  /**
   * Returns true if name of workspace is changed.
   *
   * @returns {boolean}
   */
  isNameChanged() {
    if (this.workspaceDetails && this.workspaceDetails.config) {
      return this.workspaceDetails.config.name !== this.newName;
    }
    return false;
  }

  /**
   * Updates name of workspace in config.
   *
   * @param isFormValid {boolean} true if workspaceNameForm is valid
   */
  updateName(isFormValid) {
    if (isFormValid === false || !this.isNameChanged()) {
      return;
    }

    this.copyWorkspaceDetails.config.name = this.newName;
  }

  /**
   * Returns current status of workspace.
   *
   * @returns {String}
   */
  getWorkspaceStatus() {
    let unknownStatus = 'unknown';
    if (this.isCreateMode) {
      return unknownStatus;
    }

    let workspace = this.cheWorkspace.getWorkspaceById(this.workspaceId);
    return workspace ? workspace.status : unknownStatus;
  }

  /**
   * Returns workspace details sections (tabs, example - projects)
   *
   * @returns {*}
   */
  getSections() {
    return this.workspaceDetailsService.getSections();
  }

  /**
   * Callback when stack has been changed.
   *
   * @param stack {Object}
   * @param config {object} workspace config
   */
  changeWorkspaceStack(stack, config) {
    this.stack = stack;
    this.workspaceDetails.config = config;

    // for compose recipe
    // check if there are machines without memory limit
    let defaultEnv = this.workspaceDetails.config.defaultEnv,
        environment = this.workspaceDetails.config.environments[defaultEnv];
    if (environment.recipe && environment.recipe.type === 'compose') {
      let recipeType = environment.recipe.type,
          environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
      let machines = environmentManager.getMachines(environment);
      machines.forEach((machine) => {
        if (!machine.attributes.memoryLimitBytes || machine.attributes.memoryLimitBytes < MIN_WORKSPACE_RAM) {
          environmentManager.setMemoryLimit(machine, DEFAULT_WORKSPACE_RAM);
        }
      });
      this.workspaceDetails.config.environments[defaultEnv] = environmentManager.getEnvironment(environment, machines);
    }

    this.machinesViewStatus = {};
    this.updateWorkspaceData();
  }

  /**
   * Callback when environment has been changed.
   *
   * @returns {*|promise|jQuery.promise|IPromise<T>|Promise}
   */
  updateWorkspaceConfig() {
    if (!this.isCreateMode) {
      this.editMode = !angular.equals(this.copyWorkspaceDetails.config, this.workspaceDetails.config);

      let status = this.getWorkspaceStatus();
      if (status === 'STOPPED' || status === 'STOPPING') {
        this.showApplyMessage = false;
      } else {
        this.showApplyMessage = true;
      }
    }

    let defer = this.$q.defer();
    defer.resolve();
    return defer.promise;
  }

  /**
   * Updates workspace config and restarts workspace if it's necessary
   */
  applyConfigChanges() {
    this.editMode = false;
    this.showApplyMessage = false;

    let status = this.getWorkspaceStatus();

    if (status !== 'RUNNING' && status !== 'STARTING') {
      this.doUpdateWorkspace();
      return;
    }

    this.selectedTabIndex = 0;
    this.loading = true;

    let stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(this.workspaceId, 'STOPPED');
    if (status === 'RUNNING') {
      this.stopWorkspace();
      stoppedStatusPromise.then(() => {
        return this.doUpdateWorkspace();
      }).then(() => {
        this.runWorkspace();
      });
      return;
    }

    let runningStatusPromise = this.cheWorkspace.fetchStatusChange(this.workspaceId, 'RUNNING');
    if (status === 'STARTING') {
      runningStatusPromise.then(() => {
        this.stopWorkspace();
        return stoppedStatusPromise;
      }).then(() => {
        return this.doUpdateWorkspace();
      }).then(() => {
        this.runWorkspace();
      });
    }
  }

  /**
   * Cancels workspace config changes that weren't stored
   */
  cancelConfigChanges() {
    this.editMode = false;
    this.updateWorkspaceData();
  }

  /**
   * Updates workspace info.
   */
  doUpdateWorkspace() {
    delete this.copyWorkspaceDetails.links;

    let promise = this.cheWorkspace.updateWorkspace(this.workspaceId, this.copyWorkspaceDetails);
    promise.then((data) => {
      this.workspaceName = data.config.name;
      this.updateWorkspaceData();
      this.cheNotification.showInfo('Workspace updated.');
      return this.$location.path('/workspace/' + this.namespace + '/' + this.workspaceName);
    }, (error) => {
      this.loading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Update workspace failed.');
      this.$log.error(error);
    });

    return promise;
  }

  /**
   * Generates a default workspace name
   *
   * @returns {String} name of workspace
   */
  generateWorkspaceName() {
    let name,
        iterations = 100;
    while (iterations--) {
      name = 'wksp-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4)); // jshint ignore:line
      if (!this.usedNamesList.includes(name)) {
        break;
      }
    }
    return name;
  }

  /**
   * Submit a new workspace from current workspace name, source and workspace ram
   *
   * @param source machine source
   */
  createWorkspace(source) {
    let attributes = this.stack ? {stackId: this.stack.id} : {};
    let creationPromise = this.cheWorkspace.createWorkspaceFromConfig(null, this.copyWorkspaceDetails.config, attributes);
    this.redirectAfterSubmitWorkspace(creationPromise);
  }


  /**
   * Handle the redirect for the given promise after workspace has been created
   * @param promise used to gather workspace data
   */
  redirectAfterSubmitWorkspace(promise) {
    promise.then((workspaceData) => {
      // update list of workspaces
      // for new workspace to show in recent workspaces
      this.updateRecentWorkspace(workspaceData.id);

      let infoMessage = 'Workspace ' + workspaceData.config.name + ' successfully created.';
      this.cheNotification.showInfo(infoMessage);
      this.cheWorkspace.fetchWorkspaces().then(() => {
        this.$location.path('/workspace/' + workspaceData.namespace + '/' +  workspaceData.config.name);
      });
    }, (error) => {
      let errorMessage = error.data.message ? error.data.message : 'Error during workspace creation.';
      this.cheNotification.showError(errorMessage);
    });
  }

  /**
   * Emit event to move workspace immediately
   * to top of the recent workspaces list in left navbar
   *
   * @param workspaceId
   */
  updateRecentWorkspace(workspaceId) {
    this.$rootScope.$broadcast('recent-workspace:set', workspaceId);
  }

  /**
   * Updates the workspace's environment with data entered by user.
   *
   * @param workspace workspace to update
   */
  setEnvironment(workspace) {
    if (!workspace.defaultEnv || !workspace.environments || workspace.environments.length === 0) {
      return;
    }

    let environment = workspace.environments[workspace.defaultEnv];
    if (!environment) {
      return;
    }

    let recipeType = environment.recipe.type;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    let machinesList = environmentManager.getMachines(environment);
    workspace.environments[workspace.defaultEnv] = environmentManager.getEnvironment(environment, machinesList);
  }

  //Perform workspace deletion.
  deleteWorkspace(event) {
    let confirm = this.$mdDialog.confirm()
      .title('Would you like to delete workspace \'' + this.workspaceDetails.config.name + '\'?')
      .ariaLabel('Delete workspace')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      if (this.workspaceDetails.status === 'STOPPED' || this.workspaceDetails.status === 'ERROR') {
        this.removeWorkspace();
      } else if (this.workspaceDetails.status === 'RUNNING') {
        this.cheWorkspace.stopWorkspace(this.workspaceId);
        this.cheWorkspace.fetchStatusChange(this.workspaceId, 'STOPPED').then(() => {
          this.removeWorkspace();
        });
      }
    });
  }

  removeWorkspace() {
    let promise = this.cheWorkspace.deleteWorkspaceConfig(this.workspaceId);

    promise.then(() => {
      this.$location.path('/workspaces');
    }, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Delete workspace failed.');
      this.$log.error(error);
    });

    return promise;
  }

  runWorkspace() {
    delete this.errorMessage;

    let promise = this.ideSvc.startIde(this.workspaceDetails);
    promise.then(() => {}, (error) => {
      let errorMessage;

      if (!error || !(error.data || error.error)) {
        errorMessage = 'Unable to start this workspace.';
      } else if (error.error) {
        errorMessage = error.error;
      } else if (error.data.errorCode === 10000 && error.data.attributes) {
        let attributes = error.data.attributes;

        errorMessage = 'Unable to start this workspace.' +
          ' There are ' + attributes.workspaces_count + ' running workspaces consuming ' +
          attributes.used_ram + attributes.ram_unit + ' RAM.' +
          ' Your current RAM limit is ' + attributes.limit_ram + attributes.ram_unit +
          '. This workspace requires an additional ' +
          attributes.required_ram + attributes.ram_unit + '.' +
          '  You can stop other workspaces to free resources.';
      } else {
        errorMessage = error.data.message;
      }

      this.cheNotification.showError(errorMessage);
      this.$log.error(error);

      this.errorMessage = errorMessage;
    });
  }

  stopWorkspace() {
    let promise = this.cheWorkspace.stopWorkspace(this.workspaceId);

    promise.then(() => {}, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Stop workspace failed.');
      this.$log.error(error);
    });
  }

  /**
   * Creates snapshot of workspace
   */
  createSnapshotWorkspace() {
    this.cheWorkspace.createSnapshot(this.workspaceId).then(() => {}, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Creating snapshot failed.');
      this.$log.error(error);
    });
  }

  setForm(tabId: string, form) {
    this.forms.set(tabId, form);
  }

  setTab(tabId: string) {
    this.currentTab = tabId;
  }

  /**
   * Returns false if all forms from specified tabs are valid
   *
   * @param tabIds {Array} list of tab IDs
   * @returns {boolean}
   */
  checkFormsValidity(tabIds: Array<any>) {
    return tabIds.some((tabId) => {
      let form = this.forms.get(tabId);
      return form && form.$invalid;
    });
  }

  /**
   * TODO
   */
  isCreateButtonDisabled() {
    let tabIds = ['settings', 'runtime'];
    if (!this.stack) {
      // custom stack is selected
      // so it needs to validate also 'stacks' tab
      tabIds.push('stacks');
    }

    return this.checkFormsValidity(tabIds);
  }

  /**
   * TODO
   */
  isRuntimeDisabled() {
    return (!this.stack && this.checkFormsValidity(['stacks']));
  }
}

