/*
 * Constellation - An open source and standard compliant SDI
 *
 *     http://www.constellation-sdi.org
 *
 *     Copyright 2014 Geomatys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

angular.module('CstlIndex', [
    // Angular official modules.
    'ngCookies',
    'ngResource',
    'ngRoute',
    // Libraries modules.
    'pascalprecht.translate',
    // Constellation modules.
    'cstl-directives',
    'cstl-services',
    'http-auth-interceptor'])
    
    .config(function($routeProvider, $httpProvider, $translateProvider) {
        
        // Configure routes.
        $routeProvider
            .when('/registration', {
                templateUrl: 'views/registration.html',
                controller: 'RegisterController'
            })
            .otherwise({
                templateUrl: 'views/main.html'
            });

        // Configure $http service.
        $httpProvider.defaults.useXDomain = true;
        $httpProvider.interceptors.push('AuthInterceptor');
    
        // Configure $translate service.
        $translateProvider.useStaticFilesLoader({
            prefix: 'i18n/',
            suffix: '.json'
        });
        $translateProvider.preferredLanguage('en');
        $translateProvider.useCookieStorage();
    })

    .controller('HeaderController', function($scope, $http, $cookies) {
        $http.get('app/conf').success(function(data) {
            $cookies.cstlUrl = data.cstl;
            $scope.cstlLoginUrl = data.cstl + 'spring/auth/form';
            $scope.cstlLogoutUrl = data.cstl + 'logout';
        });
    })

    .controller('LanguageController', function($scope, $translate) {
        $scope.changeLanguage = function(languageKey) {
            $translate.use(languageKey);
        };
    })

    .controller('RegisterController', function() {});
