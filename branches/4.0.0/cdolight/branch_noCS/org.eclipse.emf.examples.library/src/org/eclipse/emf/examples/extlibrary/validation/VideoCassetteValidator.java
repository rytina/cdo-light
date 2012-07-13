/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.examples.extlibrary.validation;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.examples.extlibrary.Person;

/**
 * A sample validator interface for {@link org.eclipse.emf.examples.extlibrary.VideoCassette}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface VideoCassetteValidator {
	boolean validate();

	boolean validateCast(EList<Person> value);
}
