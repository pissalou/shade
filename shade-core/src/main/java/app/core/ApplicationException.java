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
 * Recoverable failure.
 */
public class ApplicationException extends RuntimeException {
    public enum ExceptionCode { A1, B2, C3 }

    private ApplicationException(ExceptionCode errorCode, String description) {
         super("EXCEPTION " + errorCode + ": " + description);
    }

    public static class DomainLogicException extends ApplicationException {
        private DomainLogicException(String description) {
            super(ExceptionCode.A1, description);
        }
    }

    public static DomainLogicException domainValidationException(){
        return new DomainLogicException("Domain Validation");
    }
}
