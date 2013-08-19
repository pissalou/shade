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

package app.service;

import app.core.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.annotation.Inherited;
import java.util.List;

/**
 * Must be transactional
 */
//@Transactional
@Service
public class AbstractCRUDService<T extends PersistentObject> implements TransactionalService, Dao<T> {
//    public AbstractCRUDService() {
//    }

    @Override
    public T get(Class<T> clazz, Serializable id) {
        return Db.get(clazz, (Integer) id);
    }

    @Override
    public List<T> findAll(Class<T> clazz) {
        return Db.findAll(clazz);
    }

    @Override
    public ID save(T obj) {
        return obj.save();
    }

    @Override
    public void delete(T obj) {
        obj.delete();
    }
}
