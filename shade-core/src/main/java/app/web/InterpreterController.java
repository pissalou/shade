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

package app.web;

import app.commons.SpELUtils;
import app.core.Db;
import app.core.GroovyInterpreter;
import app.service.UserService;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.hibernate.Transaction;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Now using SpEl!
 * Read <link>http://groovy.codehaus.org/Embedding+a+Groovy+Console+in+a+Java+Server+Application</link>
 */

@Controller
public class InterpreterController {
    @Autowired
    UserService userService;
    @Autowired
    BeanFactory beanFactory;


    @RequestMapping("/interpreter*")
    public ModelAndView index() {
        return new ModelAndView("interpreter");
    }

    @RequestMapping("/interpreter/run")
    public ModelAndView run(@RequestParam("script") String script) {
        Transaction transaction = Db.getSession().beginTransaction();
        // Groovy Shell
//        Binding binding = new Binding();
//        binding.setVariable("userService", userService); // FIXME: go through ApplicationContext or try to use SpEl instead?
//        Object result = new GroovyShell(binding).parse(script).run();
        // Groovy Interpreter
//        Object result = GroovyInterpreter.run(script);
        // SpEl
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setBeanResolver(new BeanFactoryResolver(beanFactory));
        Object result = parser.parseExpression(script).getValue(ctx);
//        Db.flush(); // can not flush here, we get org.hibernate.HibernateException: No Hibernate Session bound to thread, and configuration does not allow creation of non-transactional one here
        transaction.commit();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("script" , script);
        map.put("result" , result != null  ? result.toString().replaceAll("\n", "<br/>").replaceAll(" ","&nbsp;") : Void.class.getSimpleName());
        return new ModelAndView("/interpreter", map);
    }
}