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

/**
 * Non-recoverable failure.
 */
public class ApplicationError extends RuntimeException {
    public enum ErrorCode { Z1, Y2, X3 }

    private ApplicationError(ErrorCode errorCode, String description) {
        super("ERROR " + errorCode + ": " + description);
    }

    public static ApplicationError somethingWentTerriblyWrong(){
        return new ApplicationError(ErrorCode.Z1, "Something went terribly wrong");
    }
}
