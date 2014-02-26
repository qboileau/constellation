'use strict';

/* Services */

cstlAdminApp.factory('AuthInterceptor', function($cookies) {
    return {
	    'request': function(config) {
	    	if ($cookies.cstlSessionId) {
	    	    config.url += ';jsessionid=' + $cookies.cstlSessionId;
	    	}
	        return config || $q.when(config);
	    }
	};
});


var context = findWebappContext();

var cstlContext = "http://localhost:8180/constellation/";

cstlAdminApp.factory('Account', ['$resource',
    function ($resource) {
        return $resource(cstlContext + 'spring/account', {}, {
        });
    }]);

cstlAdminApp.factory('Password', ['$resource',
    function ($resource) {
        return $resource('app/rest/account/change_password', {}, {
        });
    }]);

cstlAdminApp.factory('Sessions', ['$resource',
    function ($resource) {
        return $resource('app/rest/account/sessions/:series', {}, {
            'get': { method: 'GET', isArray: true}
        });
    }]);

cstlAdminApp.factory('Metrics', ['$resource',
    function ($resource) {
        return $resource(context + '/metrics/metrics', {}, {
            'get': { method: 'GET'}
        });
    }]);

cstlAdminApp.factory('LogsService', ['$resource',
    function ($resource) {
        return $resource('app/rest/logs', {}, {
            'findAll': { method: 'GET', isArray: true},
            'changeLevel':  { method: 'PUT'}
        });
    }]);


cstlAdminApp.factory('UserResource', ['$resource', '$cookies',
   function ($resource, $cookies) {
        return $resource(cstlContext+'api/1/user/:id');
}]);

cstlAdminApp.factory('webService', ['$resource',
                                     function ($resource) {
                                         return $resource(cstlContext+'api/1/admin/instances', {}, {
                                             'listAll':      {method: 'GET', isArray: false},
                                             'get':          {method: 'GET', url: cstlContext+'api/1/OGC/:type/:id'},
                                             'create':       {method: 'PUT', url: cstlContext+'api/1/OGC/:type'},
                                             'delete':       {method: 'DELETE', url: cstlContext+'api/1/OGC/:type/:id'},
                                             'restart':      {method: 'POST', url: cstlContext+'api/1/OGC/:type/:id/restart'},
                                             'start':        {method: 'POST', url: cstlContext+'api/1/OGC/:type/:id/start'},
                                             'stop':         {method: 'POST', url: cstlContext+'api/1/OGC/:type/:id/stop'},
                                             'metadata':     {method: 'GET', url: cstlContext+'api/1/OGC/:type/:id/metadata'},
                                             'updateMd':     {method: 'POST', url: cstlContext+'api/1/OGC/:type/:id/metadata'},
                                             'config':       {method: 'GET', url: cstlContext+'api/1/OGC/:type/:id/config'},
                                             'logs':         {method: 'GET', url: cstlContext+'api/1/log/:type/:id'},
                                             'capabilities': {method: 'GET', url: cstlContext+'WS/:type/:id?REQUEST=GetCapabilities&SERVICE=:type'},
                                             'layers' :      {method: 'GET', url: cstlContext+'api/1/MAP/:type/:id/layersummary/all', isArray: true},
                                             'addLayer':     {method: 'PUT', url: cstlContext+'api/1/MAP/:type/:id/layer'},
                                             'deleteLayer':  {method: 'DELETE', url: cstlContext+'api/1/MAP/:type/:id/:layerid'}
                                         });
                                     }]);

cstlAdminApp.factory('dataListing', ['$resource',
    function ($resource) {
        return $resource(cstlContext+'api/1/data/list/:filter', {}, {
            'listAll':      {method: 'GET', isArray: true},
            'listCoverage': {method: 'POST', url: cstlContext+'api/1/data/coverage/list/'},
            'pyramidData':  {method: 'POST', url: cstlContext+'api/1/data/pyramid/:id'},
            'dataFolder':   {method: 'POST', url: cstlContext+'api/1/data/datapath', isArray: true},
            'loadData':     {method: 'POST', url: cstlContext+'api/1/data/load'},
            'extension':    {method: 'POST', url: cstlContext+'api/1/data/testextension'},
            'deleteData':   {method: 'DELETE', url: cstlContext+'api/1/data/:providerid/:dataid'},
            'setMetadata':  {method: 'POST', url: cstlContext+'api/1/data/metadata'},
            'codeLists':    {method: 'GET', url: cstlContext+'api/1/data/metadataCodeLists/:lang'}
        });
    }]);

cstlAdminApp.factory('style', ['$resource',
    function ($resource) {
        return $resource(cstlContext+'api/1/SP/all/style/available', {}, {
            'listAll': { method: 'GET', isArray: false },
            'delete':  { method: 'DELETE', url: cstlContext+'api/1/SP/:provider/style/:name'},
            'link':    { method: 'POST',   url: cstlContext+'api/1/SP/:provider/style/:name/linkData'},
            'unlink':  { method: 'POST',   url: cstlContext+'api/1/SP/:provider/style/:name/unlinkData'}
        });
    }]);

cstlAdminApp.factory('provider', ['$resource',
    function ($resource) {
        return $resource(cstlContext+'api/1/provider', {}, {
            'create': {method: 'PUT', url: cstlContext+'api/1/provider/:id'},
            'delete': {method: 'DELETE', url: cstlContext+'api/1/provider/:id'}
        });
    }]);


cstlAdminApp.factory('textService', ['$http',
    function ($http){
        return {
            logs : function(type, id){
                return $http.get(cstlContext+'api/1/log/'+type+'/'+id);

            },
            capa : function(type, id){
                return $http.get(cstlContext+'WS/'+type+'/'+id+'?REQUEST=GetCapabilities&SERVICE='+type);

            }
        };
    }]);

cstlAdminApp.factory('AuthenticationSharedService', ['$rootScope', '$http', 'authService', '$base64',
    function ($rootScope, $http, authService, $base64) {
        return {
            authenticate: function() {
                $http.get(cstlContext + 'spring/login/status')
                    .success(function (data, status, headers, config) {
                        $rootScope.$broadcast('event:auth-authConfirmed');
                      //  $http.defaults.headers.common.Authorization = 'Basic ' + $base64.encode(param.username+':'+param.password);
                    });
            },
            logout: function () {
                $rootScope.authenticationError = false;
                $http.get(cstlContext + "api/1/session/logout");
                $http.defaults.headers.common.Authorization = undefined;
                $http.get(context + '/app/logout')
                    .success(function (data, status, headers, config) {
                        authService.loginCancelled();
                    });
            }
        };
    }]);


cstlAdminApp.directive('pageSwitcher', function() {
    return {
        // Applicable as element/attribute (prefer attribute for IE8 support).
        restrict: 'AE',

        // Declare a new child scope for this directive. Without this line
        // the directive scope will be the current scope, this may cause conflict
        // between multiple directive instances.
        scope: {
            onSelectPage: '&'
        },

        // Directive initialization. Note that there is no controller for this
        // directive because we don't need to expose an API to other directives.
        link: function(scope, element, attrs) {

            // Observe 'page-switcher' attribute changes.
            scope.$parent.$watch(attrs['pageSwitcher'], function(newVal) {
                watchAction(newVal);
            }, true);

            // Handle attributes changes.
            function watchAction(newVal) {
                var page  = newVal.page  || 1,
                    size  = newVal.size  || 10,
                    count = newVal.count || 0;

                // Compute pagination.
                var totalPages = Math.ceil(count / size) || 1,
                    prevCount  = page - 1,
                    nextCount  = totalPages - page,
                    minPage    = page - Math.min(4 - Math.min(2, nextCount), prevCount),
                    maxPage    = page + Math.min(4 - Math.min(2, prevCount), nextCount);

                // Update scope.
                scope.totalPages = totalPages;
                scope.page       = page;
                scope.indexes    = [];
                for (var i = minPage; i <= maxPage; i++) {
                    scope.indexes.push(i);
                }
            }

            // Page select action.
            scope.selectPage = function(page) {
                if (scope.page !== page && page > 0 && page <= scope.totalPages) {
                    scope.onSelectPage({page: page});
                }
            };
        },

        // Replace the element with the following template.
        replace: true,

        // Component HTML template.
        template:
            '<ul class="pagination">' +
                '<li><a ng-click="selectPage(page - 1)">&laquo;</a></li>' +
                '<li ng-repeat="index in indexes" ng-class="{active: index == page}"><a ng-click="selectPage(index)">{{index}}</a></li>' +
                '<li><a ng-click="selectPage(page + 1)">&raquo;</a></li>' +
                '</ul>'
    };
});

cstlAdminApp.service('$dashboard', function($filter) {
    return function($scope, fullList) {

        $scope.fullList = fullList || [];
        $scope.dataList = $scope.dataList || [];
        $scope.filtertext = $scope.filtertext || "";
        $scope.filtertype = $scope.filtertype || "";
        $scope.ordertype = $scope.ordertype || "Name";
        $scope.orderreverse = $scope.orderreverse || false;
        $scope.countdata = $scope.countdata || 0;
        $scope.nbbypage = $scope.nbbypage || 10;
        $scope.currentpage = $scope.currentpage || 1;
        $scope.selected = $scope.selected || null;
        $scope.exclude = $scope.exclude || [];

        // Dashboard methods
        $scope.displayPage = function(page) {
            var array = $filter('filter')($scope.fullList, {'Type':$scope.filtertype, '$': $scope.filtertext});
            array = $filter('orderBy')(array, $scope.ordertype, $scope.orderreverse);

            var list = [];
            for (var i = 0; i < array.length; i++) {
                var found = false;
                for (var j = 0; j < $scope.exclude.length; j++) {
                    if ($scope.exclude[j].Name === array[i].Name) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    list.push(array[i]);
                }
            }

            var start = (page - 1) * $scope.nbbypage;

            $scope.currentpage = page;
            $scope.countdata = list.length;
            $scope.dataList = list.splice(start, $scope.nbbypage);
            $scope.selected = null;
        };

        $scope.select = function(item) {
            $scope.selected = item;
        };

        $scope.$watch('nbbypage', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('filtertext', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('filtertype', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('fullList', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('ordertype', function() {
            $scope.displayPage($scope.currentpage);
        });
        $scope.$watch('orderreverse', function() {
            $scope.displayPage($scope.currentpage);
        });
    };
});

cstlAdminApp.service('$growl', function() {
    /**
     * Displays a notification with the specified title and text.
     *
     * @param type  - {string} the notification type (info|error|success|warning)
     * @param title - {string} the notification title
     * @param msg   - {string} the notification message
     */
    return function(type, title, msg) {
        if (type === 'info') {
            $.growl({title: title, message: msg});
        } else if (type === 'error') {
            $.growl.error({title: title, message: msg});
        } else if (type === 'success') {
            $.growl.notice({title: title, message: msg});
        } else if (type === 'warning') {
            $.growl.warning({title: title, message: msg});
        }
    };
});

cstlAdminApp.service('$uploadFiles', function() {
    return {
        files : {file: null, mdFile: null}
    };
});
