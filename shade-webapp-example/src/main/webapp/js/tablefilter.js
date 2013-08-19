/*
 * Copyright (C) 2013 Pascal Mazars
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//Table free-text filter. Uses a encapsulation-pattern.
function MyTableFilter() {}
(function() {
 	var DEBUG = false;
	var THIS = this;
	this.options = { caseSensitive: false, startsWith: true }; //Not implemented yet

	function log(message) {
		if (DEBUG) {
			jQuery('#log').append(message + '<br/>');
		}
	}

	this.matchesSearch = function(candidateStr, searchStr) {
		var result = candidateStr.search(new RegExp('\\b' + searchStr, 'i')) > -1;
		log(candidateStr + ' ' + searchStr + '= ' +result);
		return result;
	};

	this.filter = function(table, searchExpr) {
        log('filter ' + table + ' with searchExpr ' + searchExpr);
		//Find matches (find('tobdy tr') does not work, I don't understand why)
		table.find('tbody').find('tr').each(function() {
			var row = jQuery(this);

			if (searchExpr.length > 0) {
				var matches = new Array();
				row.find('td').each(function() {
					var cell = jQuery(this);
					var i = 0;
					if (THIS.matchesSearch(cell.text(), searchExpr)) {
						matches[i++] = cell;
					}
				});
				//Show/hide unmatched rows
				row.toggle(matches.length > 0);
//				for (var j in matches) {
//					matches[j].addClass('selected');
//				}
			} else {
//				row.find('*').removeClass('selected');
				row.show();
			}
		});
	};
}).apply(MyTableFilter);