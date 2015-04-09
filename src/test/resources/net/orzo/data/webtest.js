(function (scope) {
    'use strict';

    scope.modifyDivOldElements = function () {
        scope._lib.web.queryPage(scope.startFrom, 'div.old', function (item) {
            item.addClass('foo');
        });        
    };

    scope.modifyDivOldElementsWithoutCallback = function () {
        var items = scope._lib.web.queryPage(scope.startFrom, 'div.old', null);        

        items.forEach(function (item) {
            item.addClass('foo');
        });
    };

}(this));