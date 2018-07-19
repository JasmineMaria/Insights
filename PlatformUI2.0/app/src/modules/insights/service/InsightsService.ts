/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

/// <reference path="../../../_all.ts" />

module ISightApp {
    export interface IInsightsService {
        getInsightsDetails(schedule : string): ng.IPromise<any>;
    }

    export class InsightsService implements IInsightsService {
        static $inject = ['$resource', '$q', '$cookies', 'restCallHandlerService'];
       

         constructor(private $resource, private $q, private $cookies, private restCallHandlerService: IRestCallHandlerService) {
        }

        getInsightsDetails(schedule : string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("INSIGHTS_GET",{'schedule':schedule});
        }
    }
}