/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.examples.extlibrary.validation;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.util.FeatureMap;

import org.eclipse.emf.examples.extlibrary.Book;
import org.eclipse.emf.examples.extlibrary.Borrower;
import org.eclipse.emf.examples.extlibrary.Employee;
import org.eclipse.emf.examples.extlibrary.Item;
import org.eclipse.emf.examples.extlibrary.Library;
import org.eclipse.emf.examples.extlibrary.Writer;

/**
 * A sample validator interface for {@link org.eclipse.emf.examples.extlibrary.Library}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface LibraryValidator {
	boolean validate();

	boolean validateName(String value);
	boolean validateWriters(EList<Writer> value);
	boolean validateEmployees(EList<Employee> value);
	boolean validateBorrowers(EList<Borrower> value);
	boolean validateStock(EList<Item> value);
	boolean validateBooks(EList<Book> value);
	boolean validateBranches(EList<Library> value);
	boolean validateParentBranch(Library value);
	boolean validatePeople(FeatureMap value);
}