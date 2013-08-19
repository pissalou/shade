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

package app.commons;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;

/**
 *
 * @author: pascal
 */
public class SpELUtils {

    public static Object parseExpression(String expression) {
        return parseExpression(expression, null);
    }

    public static Object parseExpression(String expression, Object rootObject) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext ctx = new StandardEvaluationContext();
//        ctx.setBeanResolver();
        ctx.setRootObject(rootObject);
//        ctx.setVariable("root", rootObject);
        return parser.parseExpression(expression).getValue(ctx);
    }

    public static void main(String[] args) {
        final List<Integer> root = new ArrayList<Integer>() {{
            this.add(new Integer(1));
            this.add(new Integer(2));
            this.add(new Integer(3));
        }};
        System.out.println(parseExpression("#this", root));
    }
}
