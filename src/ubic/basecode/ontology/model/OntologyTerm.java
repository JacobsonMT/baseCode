/*
 * The Gemma project
 * 
 * Copyright (c) 2007 Columbia University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.basecode.ontology.model;

import java.util.Collection;

/**
 * @author Paul
 * @version $Id$
 */
public interface OntologyTerm extends OntologyResource {

    public Collection<String> getAlternativeIds();

    public Collection<AnnotationProperty> getAnnotations();

    /**
     * @param direct return only the immediate children; if false, return all of them down to the leaves.
     * @return
     */
    public Collection<OntologyTerm> getChildren( boolean direct );

    public String getComment();

    public Collection<OntologyIndividual> getIndividuals();

    public Collection<OntologyIndividual> getIndividuals( boolean direct );

    public String getLocalName();

    public Object getModel();

    /**
     * Note that any restriction superclasses are not returned, unless they are has_proper_part
     * 
     * @param direct
     * @return
     */
    public Collection<OntologyTerm> getParents( boolean direct );

    public Collection<OntologyRestriction> getRestrictions();

    public String getTerm();

    @Override
    public String getUri();

    public boolean isRoot();

    /** check to see if the term is obsolete, if it is it should not be used */
    public boolean isTermObsolete();

}
