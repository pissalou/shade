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

package app.core;

import java.util.Comparator;

/**
 * @author pma
 */
public class EventComparator implements Comparator<AbstractEvent> {

    /**
     * It is important we never return 0.
     * @param event1
     * @param event2
     * @return
     */
    @Override
    public int compare(AbstractEvent event1, AbstractEvent event2) {
        int result = event1.getEmployee().getEmployeeNo() - event2.getEmployee().getEmployeeNo();
        if (result != 0)
            return result;
        result = event1.getPriority() - event2.getPriority();
        if (result != 0)
            return result;
        return event1.getInterval().getStart().compareTo(event2.getInterval().getStart());
    }
}
