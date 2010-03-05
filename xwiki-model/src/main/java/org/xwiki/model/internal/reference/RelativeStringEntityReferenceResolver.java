/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.model.internal.reference;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

/**
 * Resolve {@link org.xwiki.model.reference.EntityReference} objects from its string representations. This
 * implementation doesn't generate absolute references; instead it stores the the representation into an
 * {@link EntityReference} object (ie if the representation is a Document reference and it has only a page name
 * specified, then a single EntityReference of type DOCUMENT will be returned, with no Space and Wiki references).
 * This is useful in cases when we need to store a reference relative to another reference (for example for storing
 * Parent references in a Document, since we want these references to stay relative if the user has specified a
 * relative reference, and absolute if the user has specified an absolute reference).
 * </p>
 * In other words, this implementation just transforms a String representation into a {@link EntityReference}
 * representation without resolving any missing parts (space, wiki, etc).
 *  
 * @version $Id$
 * @since 2.2.3
 */
@Component("relative")
public class RelativeStringEntityReferenceResolver extends AbstractStringEntityReferenceResolver
{
    /**
     * {@inheritDoc}
     * @see AbstractStringEntityReferenceResolver#getDefaultValuesForType(org.xwiki.model.EntityType) 
     */
    @Override protected String getDefaultValuesForType(EntityType type)
    {
        // Return null to signify to the generic algorithm that we don't want to generate references with default
        // values.
        return null;
    }

    /**
     * {@inheritDoc}
     * @see AbstractStringEntityReferenceResolver#resolve(String, org.xwiki.model.EntityType)
     */
    @Override public EntityReference resolve(String entityReferenceRepresentation, EntityType type)
    {
        return super.resolve(entityReferenceRepresentation, type);
    }
}
