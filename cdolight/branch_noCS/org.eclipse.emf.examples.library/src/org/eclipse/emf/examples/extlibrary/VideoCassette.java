/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.examples.extlibrary;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Video Cassette</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.VideoCassette#getCast <em>Cast</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.emf.examples.extlibrary.EXTLibraryPackage#getVideoCassette()
 * @model
 * @generated
 */
public interface VideoCassette extends AudioVisualItem {
	/**
	 * Returns the value of the '<em><b>Cast</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.examples.extlibrary.Person}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cast</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cast</em>' reference list.
	 * @see org.eclipse.emf.examples.extlibrary.EXTLibraryPackage#getVideoCassette_Cast()
	 * @model
	 * @generated
	 */
	EList<Person> getCast();

} // VideoCassette
