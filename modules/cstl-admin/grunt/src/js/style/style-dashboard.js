/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2014, Geomatys
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details..
 */

angular.module('cstl-style-dashboard', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('StylesController', function($scope,Dashboard,style,Growl,StyleSharedService,$modal,$window,
                                             $cookieStore,provider) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.hideScroll = true;

        $scope.styleCtrl = {
            cstlUrl:$cookieStore.get('cstlUrl'),
            currentStyleId:null,
            currentDataId:null,
            currentLayerId:null,
            currentTabInfo:'view'
        };

        $scope.initStyleDashboard = function() {
            style.listAll({provider: 'sld'},function(response) {//success
                    Dashboard($scope, response.styles, true);
                    $scope.wrap.filtertype = "";
                    $scope.wrap.ordertype = "Name";
                    $scope.wrap.filtertext='';
                    $scope.wrap.orderreverse=false;
                    setTimeout(function(){
                        $scope.previewStyledData(null,false);
                    },300);
                },function() {//error
                    Growl('error','Error','Unable to show styles list!');
                }
            );
            angular.element($window).bind("scroll", function() {
                if (this.pageYOffset < 220) {
                    $scope.hideScroll = true;
                } else {
                    $scope.hideScroll = false;
                }
                $scope.$apply();
            });
        };

        /**
         * Reset filters for dashboard
         */
        $scope.resetFilters = function(){
            style.listAll({provider: 'sld'},function(response) {//success
                    Dashboard($scope, response.styles, true);
                    $scope.wrap.filtertype = "";
                    $scope.wrap.ordertype = "Name";
                    $scope.wrap.filtertext='';
                    $scope.wrap.orderreverse=false;
                },function() {//error
                    Growl('error','Error','Unable to show styles list!');
                }
            );
        };

        /**
         * Select tab in information block.
         */
        $scope.selectTabInfo = function(item) {
            $scope.styleCtrl.currentTabInfo = item;
            if(item==='view') {
                setTimeout(function () {
                    $scope.previewStyledData(null, false);
                }, 300);
            }
        };

        /**
         * Proceed to remove the selected styles from dashboard.
         */
        $scope.deleteStyle = function() {
            var dlg = $modal.open({
                templateUrl: 'views/modal-confirm.html',
                controller: 'ModalConfirmController',
                resolve: {
                    'keyMsg':function(){return "dialog.message.confirm.delete.style";}
                }
            });
            dlg.result.then(function(cfrm){
                if(cfrm){
                    var styleName = $scope.selected.Name;
                    var providerId = $scope.selected.Provider;
                    style.delete({provider: providerId, name: styleName}, {},
                        function() {
                            Growl('success', 'Success', 'Style ' + styleName + ' successfully deleted');
                            style.listAll({provider: 'sld'}, function(response) {
                                Dashboard($scope, response.styles, true);
                                $scope.selected=null;
                                $scope.previewStyledData(null,false);
                            });
                        },
                        function() {
                            Growl('error', 'Error', 'Style ' + styleName + ' deletion failed');
                        }
                    );
                }
            });
        };

        /**
         * Proceed to open modal SLD editor to edit the selected style
         */
        $scope.editStyle = function() {
            var styleName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            style.get({provider: providerId, name: styleName}, function(response) {
                StyleSharedService.showStyleEdit($scope, response);
            });
        };

        $scope.editStyleWithLinkedData = function(selectedData) {
            style.get({provider: $scope.selected.Provider, name: $scope.selected.Name}, function(response) {
                StyleSharedService.editLinkedStyle($scope, response,selectedData);
            });
        };

        /**
         * Toggle up and down the selected item
         */
        $scope.toggleUpDownSelected = function() {
            var $header = $('#stylesDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

        /**
         * Open sld editor modal to create a new style.
         */
        $scope.showStyleCreate = function() {
            StyleSharedService.showStyleCreate($scope);
        };

        $scope.previewStyledData = function(data,isLayer) {
            if (StyleDashboardViewer.map) {
                StyleDashboardViewer.map.setTarget(undefined);
            }
            StyleDashboardViewer.initConfig();
            StyleDashboardViewer.fullScreenControl = true;
            var selectedStyle = $scope.selected;
            if(selectedStyle) {
                $scope.styleCtrl.currentStyleId=selectedStyle.Id;
                var dataToShow;
                if(data){
                    dataToShow = data;
                    if(isLayer){
                        $scope.styleCtrl.currentLayerId=dataToShow.Id;
                        $scope.styleCtrl.currentDataId=null;
                    }else {
                        $scope.styleCtrl.currentDataId=dataToShow.Id;
                        $scope.styleCtrl.currentLayerId=null;
                    }
                }else if(selectedStyle.dataList && selectedStyle.dataList.length>0){
                    dataToShow = selectedStyle.dataList[0];
                    $scope.styleCtrl.currentDataId=dataToShow.Id;
                    $scope.styleCtrl.currentLayerId=null;
                }else if(selectedStyle.layersList && selectedStyle.layersList.length>0) {
                    dataToShow = selectedStyle.layersList[0];
                    $scope.styleCtrl.currentLayerId=dataToShow.Id;
                    $scope.styleCtrl.currentDataId=null;
                }else {
                    // the style is not used by any data or layer.
                    // So we should use a default data depending on style type vector or raster.
                    $scope.styleCtrl.currentLayerId=null;
                    $scope.styleCtrl.currentDataId=null;
                    dataToShow = {
                        Namespace:null,
                        Name:null,
                        Provider:null,
                        Type:selectedStyle.Type
                    };
                    if(selectedStyle.Type && selectedStyle.Type.toLowerCase() === 'vector'){
                        dataToShow.Provider = 'generic_shp';
                        dataToShow.Name = 'CNTR_RG_60M_2006';
                    }else {
                        dataToShow.Provider = 'generic_world_tif';
                        dataToShow.Name = 'cloudsgrey';
                    }
                }
                if(dataToShow) {
                    var layerName;
                    if (dataToShow.Namespace) {
                        layerName = '{' + dataToShow.Namespace + '}' + dataToShow.Name;
                    } else {
                        layerName = dataToShow.Name;
                    }
                    var providerId = dataToShow.Provider;
                    var pyramidProviderId = dataToShow.PyramidConformProviderId;
                    var type = dataToShow.Type.toLowerCase();
                    var layerData = StyleDashboardViewer.createLayerWithStyle($scope.styleCtrl.cstlUrl,layerName,
                                pyramidProviderId?pyramidProviderId:providerId,
                                selectedStyle.Name,
                                null,null,type!=='vector');
                    //to force the browser cache reloading styled layer.
                    layerData.get('params').ts=new Date().getTime();
                    StyleDashboardViewer.layers = [layerData];
                    provider.dataGeoExtent({},{values: {'providerId':providerId,'dataId':layerName}},
                        function(response) {//success
                            var bbox = response.boundingBox;
                            if (bbox) {
                                StyleDashboardViewer.extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                            }
                            StyleDashboardViewer.initMap('stylePreviewMap');
                        }, function() {//error
                            // failed to find an extent bbox, just load the full map
                            StyleDashboardViewer.initMap('stylePreviewMap');
                        }
                    );
                } else {
                    StyleDashboardViewer.initMap('stylePreviewMap');
                }
            }else {
                $scope.styleCtrl.currentStyleId=null;
                StyleDashboardViewer.initMap('stylePreviewMap');
            }
        };

        $scope.truncate = function(small, text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (small === true && text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else if (small === false && text.length > 60) {
                        return text.substr(0, 60) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small === true && text.length > 15) {
                        return text.substr(0, 15) + "...";
                    } else if (small === false && text.length > 45) {
                        return text.substr(0, 45) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 32) {
                        return text.substr(0, 32) + "...";
                    } else {return text;}
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else {return text;}
                }
            }
        };
    });