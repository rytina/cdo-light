/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.examples.extlibrary;

import org.eclipse.emf.cdo.CDOObject;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Lendable</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.Lendable#getCopies <em>Copies</em>}</li>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.Lendable#getBorrowers <em>Borrowers</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.emf.examples.extlibrary.EXTLibraryPackage#getLendable()
 * @model interface="true" abstract="true"
 * @extends CDOObject
 * @generated
 */
public interface Lendable extends CDOObject {
	/**
	 * Returns the value of the '<em><b>Copies</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Copies</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Copies</em>' attribute.
	 * @see #setCopies(int)
	 * @see org.eclipse.emf.examples.extlibrary.EXTLibraryPackage#getLendable_Copies()
	 * @model required="true"
	 * @generated
	 */
	int getCopies();

	/**
	 * Sets the value of the '{@link org.eclipse.emf.examples.extlibrary.Lendable#getCopies <em>Copies</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Copies</em>' attribute.
	 * @see #getCopies()
	 * @generated
	 */
	void setCopies(int value);

	/**
	 * Returns the value of the '<em><b>Borrowers</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.examples.extlibrary.Borrower}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.emf.examples.extlibrary.Borrower#getBorrowed <em>Borrowed</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Borrowers</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Borrowers</em>' reference list.
	 * @see org.eclipse.emf.examples.extlibrary.EXTLibraryPackage#getLendable_Borrowers()
	 * @see org.eclipse.emf.examples.extlibrary.Borrower#getBorrowed
	 * @model opposite="borrowed" ordered="false"
	 * @generated
	 */
	EList<Borrower> getBorrowers();

} // Lendable
