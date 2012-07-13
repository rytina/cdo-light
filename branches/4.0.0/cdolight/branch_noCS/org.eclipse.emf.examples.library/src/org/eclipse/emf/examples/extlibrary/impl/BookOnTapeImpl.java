/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.examples.extlibrary.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.examples.extlibrary.BookOnTape;
import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.eclipse.emf.examples.extlibrary.Person;
import org.eclipse.emf.examples.extlibrary.Writer;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Book On Tape</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.impl.BookOnTapeImpl#getReader <em>Reader</em>}</li>
 *   <li>{@link org.eclipse.emf.examples.extlibrary.impl.BookOnTapeImpl#getAuthor <em>Author</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BookOnTapeImpl extends AudioVisualItemImpl implements BookOnTape {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BookOnTapeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EXTLibraryPackage.Literals.BOOK_ON_TAPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Person getReader() {
		return (Person)eGet(EXTLibraryPackage.Literals.BOOK_ON_TAPE__READER, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setReader(Person newReader) {
		eSet(EXTLibraryPackage.Literals.BOOK_ON_TAPE__READER, newReader);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Writer getAuthor() {
		return (Writer)eGet(EXTLibraryPackage.Literals.BOOK_ON_TAPE__AUTHOR, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAuthor(Writer newAuthor) {
		eSet(EXTLibraryPackage.Literals.BOOK_ON_TAPE__AUTHOR, newAuthor);
	}

} //BookOnTapeImpl
