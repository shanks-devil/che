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
 * @name workspaces.workspace.stacks.controller:WorkspaceStacksController
 * @description This class is handling the controller for stacks selection
 * @author Oleksii Kurinnyi
 */

const DEFAULT_WORKSPACE_RAM: number = 2 * Math.pow(1024,3);

export class WorkspaceStacksController {
  $scope;
  cheWorkspace:any;

  recipeUrl: string;
  recipeScript: string;
  recipeFormat: string;

  stack: any = null;
  isCustomStack: boolean = false;
  selectSourceOption: string;

  workspaceName: string;
  workspaceStackOnChange: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope, cheWorkspace) {
    this.cheWorkspace = cheWorkspace;

    $scope.$watch(() => {return this.recipeScript;}, () => {
      if (this.isCustomStack) {
        this.cheStackLibrarySelecter(null);
      }
    });
    $scope.$watch(() => {return this.recipeUrl}, () => {
      if (this.isCustomStack) {
        this.cheStackLibrarySelecter(null);
      }
    });
    $scope.$watch(() => {return this.recipeFormat}, () => {
      if (this.isCustomStack) {
        this.cheStackLibrarySelecter(null);
      }
    });
  }

  /**
   * Callback when tab has been change.
   *
   * @param tabName  the select tab name
   */
  setStackTab(tabName) {
    if (tabName === 'custom-stack') {
      this.cheStackLibrarySelecter(null);
      this.isCustomStack = true;
    }
  }

  /**
   * Callback when stack has been set.
   *
   * @param stack  the selected stack
   */
  cheStackLibrarySelecter(stack) {
    if (stack) {
      this.isCustomStack = false;
      this.recipeUrl = null;
    }
    this.stack = stack;

    let source = this.getSource();
    let config = this.buildWorkspaceConfig(source);

    this.workspaceStackOnChange({
      stack: stack,
      config: config
    });
  }

  /**
   * Builds workspace config.
   *
   * @param source
   * @returns {config}
   */
  buildWorkspaceConfig(source) {
    let stackWorkspaceConfig = this.stack ? this.stack.workspaceConfig : {};
    let workspaceConfig = this.cheWorkspace.formWorkspaceConfig(stackWorkspaceConfig, this.workspaceName, source, DEFAULT_WORKSPACE_RAM);
    return workspaceConfig;
  }

  /**
   * Returns stack source.
   *
   * @returns {object}
   */
  getSource() {
    let source: any = {};
    source.type = 'dockerfile';
    //User provides recipe URL or recipe's content:
    if (this.isCustomStack) {
      this.stack = null;
      source.type = 'environment';
      source.format = this.recipeFormat;
      if (this.recipeUrl && this.recipeUrl.length > 0) {
        source.location = this.recipeUrl;
      } else {
        source.content = this.recipeScript;
      }
    } else if (this.stack) {
      //check predefined recipe location
      if (this.stack && this.stack.source && this.stack.source.type === 'location') {
        this.recipeUrl = this.stack.source.origin;
        source.location = this.recipeUrl;
      } else {
        source = this.getSourceFromStack(this.stack);
      }
    }
    return source;
  }

  /**
   * Detects machine source from pointed stack.
   *
   * @param stack to retrieve described source
   * @returns {source} machine source config
   */
  getSourceFromStack(stack) {
    let source: any = {};
    source.type = 'dockerfile';

    switch (stack.source.type.toLowerCase()) {
      case 'image':
        source.content = 'FROM ' + stack.source.origin;
        break;
      case 'dockerfile':
        source.content = stack.source.origin;
        break;
      default:
        throw 'Not implemented';
    }

    return source;
  }
}
