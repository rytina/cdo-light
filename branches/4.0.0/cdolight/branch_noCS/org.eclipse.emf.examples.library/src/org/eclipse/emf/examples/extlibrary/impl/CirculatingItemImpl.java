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
import org.eclipse.emf.examples.extlibrary.CirculatingItem;
import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.eclipse.emf.examples.extlibrary.Lendable;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Circulating Item</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.impl.CirculatingItemImpl#getCopies <em>Copies</em>}</li>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.impl.CirculatingItemImpl#getBorrowers <em>Borrowers</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class CirculatingItemImpl extends ItemImpl implements CirculatingItem {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CirculatingItemImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EXTLibraryPackage.Literals.CIRCULATING_ITEM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCopies() {
		return (Integer)eGet(EXTLibraryPackage.Literals.LENDABLE__COPIES, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCopies(int newCopies) {
		eSet(EXTLibraryPackage.Literals.LENDABLE__COPIES, newCopies);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<Borrower> getBorrowers() {
		return (EList<Borrower>)eGet(EXTLibraryPackage.Literals.LENDABLE__BORROWERS, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == Lendable.class) {
			switch (derivedFeatureID) {
				case EXTLibraryPackage.CIRCULATING_ITEM__COPIES: return EXTLibraryPackage.LENDABLE__COPIES;
				case EXTLibraryPackage.CIRCULATING_ITEM__BORROWERS: return EXTLibraryPackage.LENDABLE__BORROWERS;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == Lendable.class) {
			switch (baseFeatureID) {
				case EXTLibraryPackage.LENDABLE__COPIES: return EXTLibraryPackage.CIRCULATING_ITEM__COPIES;
				case EXTLibraryPackage.LENDABLE__BORROWERS: return EXTLibraryPackage.CIRCULATING_ITEM__BORROWERS;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

} //CirculatingItemImpl
