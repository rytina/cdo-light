/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.examples.extlibrary.impl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.eclipse.emf.examples.extlibrary.Person;
import org.eclipse.emf.examples.extlibrary.VideoCassette;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Video Cassette</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.impl.VideoCassetteImpl#getCast <em>Cast</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class VideoCassetteImpl extends AudioVisualItemImpl implements VideoCassette {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VideoCassetteImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EXTLibraryPackage.Literals.VIDEO_CASSETTE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<Person> getCast() {
		return (EList<Person>)eGet(EXTLibraryPackage.Literals.VIDEO_CASSETTE__CAST, true);
	}

} //VideoCassetteImpl
