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

//Create a suggestion-box out of a select.
// Uses an encapsulation-pattern.
function MySuggest() {}
(function() {
 	var DEBUG = true;
    var EXCLUDE_FIRST_OPTION=true;
	var THIS = this;
	this.options = { caseSensitive: false, startsWith: true, firstOptionExcluded: true }; //Not implemented yet
    var ARROW_UP = 38;
    var ARROW_DOWN = 40;
    var BACKSPACE = 8;

	function log(message, sameLine) {
		if (DEBUG) {
			var log = jQuery('#log');
            log.append(message);
            if (!sameLine) {
                log.append('<br/>');
            }
		}
	}

    String.prototype.startsWith = function(text) {
        return this.indexOf(text) == 0;
    };

    this.loadSuggest = function(select) {
        log("Loading suggest...");

        //Creation
        var suggestInput = jQuery('<input type="text" />');
        suggestInput.width(select.width());
        var suggestList = jQuery('<ul class="suggest-list" style="display: none; position: absolute;"/>');
        select.find('option').each(function(index) {
           var option = jQuery(this);
           if (index != 0) {
               suggestList.append('<li><a href="#" onclick="MySuggest.selectSuggestion(' + index + '); return false;">' + option.text() + '</a></li>');
               if (option.attr('selected')) {
                   log("Selected option: " + option.val());
                   suggestInput.val(option.val());
               }
           }
        });
        var suggestButton = jQuery('<a href="#" tabindex="-1" onclick="MySuggest.toggleSuggestions(); return false;" style="position:relative; right:20px; text-decoration: none"><img src="images/dropdown.jpg" alt="v" style="border: 0" /></a>');

        //Interaction
        // suggestInput.focusin(function() { suggestList.show(); });
        suggestInput.click(function() { MySuggest.toggleSuggestions(); });
        // suggestInput.focusout(function() { suggestList.hide(); });

        suggestInput.keyup(function(eventData) {
            var suggestText = jQuery(this).attr('value');
            log("Keyup: " + eventData.keyCode);
            if (eventData.keyCode == ARROW_DOWN) {
                THIS.showSuggestions();
            } else {
            //log("suggestText=" + suggestText);
                select.prop('selectedIndex', 0); // reset selected option
                if (suggestText.length > 0) {
                    //Find match in select
                    log("compare suggestText=" + suggestText + " with options=", true);
                    select.find('option').each(function(index) {
                       var option = jQuery(this);
                       log(option.text() + ", ", true);
                       if (index != 0 && option.text() == suggestText) {
                           log(" match found!");
                           //option.attr('selected', true);
                           THIS.selectSuggestion(index);
                           return false;
                       }
                    });
                    log(" NO match found");
                }
                //Update suggestList
                log("update suggestions suggestText=" + suggestText);
                suggestList.find('li').each(function(index) {
                    var suggestion = jQuery(this);
                    suggestion.removeClass('selected');
                    var isMatch = suggestion.text().startsWith(suggestText);
                    suggestion.toggle(isMatch);
                    suggestion.toggleClass('disabled', isMatch);
                });
                //log(select.prop('selectedIndex'));
                validate(select, suggestInput, suggestList);
            }
        });
        suggestList.keyup(function(eventData) {
            log("Keyup list: " + eventData.keyCode);
            // increment or decrement selected option
            if (eventData.keyCode == ARROW_DOWN) {
                var selectedSuggest = suggestList.find('li.selected');
                var newSelectedSuggest = selectedSuggest.next('li').size() > 0  ? selectedSuggest.next('li') : selectedSuggest;
                newSelectedSuggest.find("a").focus();
                selectedSuggest.removeClass('selected');
                newSelectedSuggest.addClass('selected');
            } else if (eventData.keyCode == ARROW_UP) {
                var selectedSuggest = suggestList.find('li.selected');
//                log("selectedSuggest.prevUntil('li')=" + selectedSuggest.prev());
                var newSelectedSuggest = selectedSuggest.prev('li').size() > 0  ? selectedSuggest.prev('li') : selectedSuggest;
                newSelectedSuggest.find("a").focus();
                selectedSuggest.removeClass('selected');
                newSelectedSuggest.addClass('selected');
            }
        });
        suggestList.keydown(function(eventData) {
            if (eventData.keyCode == BACKSPACE) {
                THIS.hideSuggestions();
                return false;
            }
        });
        //Layout
        //log(select.attr('name'));
        //log(select.prop('selectedIndex'));
        var wrapper = jQuery('<span class="suggest" />');
        select.after(wrapper);
        wrapper.append(suggestInput);
        wrapper.append(suggestButton);
        suggestList.offset({ top: suggestInput.offset().top + suggestInput.outerHeight(), left: suggestInput.offset().left});
        suggestList.width(suggestInput.outerWidth());
        wrapper.append(suggestList);
//        select.hide();
    }

    function validate(select, suggestInput, suggestList) {
        var availableSuggestionCount = suggestList.find('li.disabled').size();
        log("Available suggestions=" + availableSuggestionCount);
        suggestInput.css('background-color', select.prop('selectedIndex') > 0 ? 'lightGreen' : availableSuggestionCount > 0 ? 'transparent' : 'pink');
    }

    this.showSuggestions = function() {
        THIS.toggleSuggestions(true);
    }

    this.hideSuggestions = function() {
        THIS.toggleSuggestions(false);
    }

//    this.isVisible = function() {
//        return jQuery('ul li:first:visible').size() > 0;
//    }

    this.toggleSuggestions = function(showOrHide) {
        // Show all or just the current available?
        var suggestList = jQuery('ul');
        suggestList.toggle(showOrHide);
        if (suggestList.find('li.selected').size() == 0) {
            var firstSuggest = suggestList.find('li:first:visible');
            if (firstSuggest) {
                firstSuggest.find("a").focus();
                firstSuggest.addClass('selected');
            }
        }
    }

    this.selectSuggestion = function(index) {
        var option = jQuery('select option:nth-child(' + (index + 1) + ')'); // 1-based not 0
        option.attr('selected', true);
        var suggestInput = jQuery('input[type=text]');
        suggestInput.val(option.text());
        suggestInput.css('background-color', 'lightGreen');
        var suggestList = jQuery('ul');
        suggestList.find('li').removeClass('selected');
        suggestList.find('li:nth-child(' + index + ')').addClass('selected');
        suggestList.hide();
        suggestInput.focus(); // necessary?
    }

}).apply(MySuggest);