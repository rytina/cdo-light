/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.examples.extlibrary.impl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.examples.extlibrary.Borrower;
import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.eclipse.emf.examples.extlibrary.Lendable;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Borrower</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.impl.BorrowerImpl#getBorrowed <em>Borrowed</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BorrowerImpl extends PersonImpl implements Borrower {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BorrowerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EXTLibraryPackage.Literals.BORROWER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<Lendable> getBorrowed() {
		return (EList<Lendable>)eGet(EXTLibraryPackage.Literals.BORROWER__BORROWED, true);
	}

} //BorrowerImpl
