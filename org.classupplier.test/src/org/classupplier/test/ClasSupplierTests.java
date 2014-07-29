package org.classupplier.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.classupplier.ClasSupplier;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.query.conditions.eobjects.IEObjectSource;
import org.eclipse.emf.query.conditions.eobjects.structuralfeatures.EObjectAttributeValueCondition;
import org.eclipse.emf.query.conditions.strings.StringValue;
import org.eclipse.emf.query.statements.FROM;
import org.eclipse.emf.query.statements.IQueryResult;
import org.eclipse.emf.query.statements.SELECT;
import org.eclipse.emf.query.statements.WHERE;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ClasSupplierTests extends AbstractTests {

	@Test
	public void creationOfTheClass() {
		EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;
		EPackage ePackage = createEPackage("reader", "1.0");
		EClass eClass = ecoreFactory.createEClass();
		eClass.setName("Book");
		EAttribute pageAttr = ecoreFactory.createEAttribute();
		pageAttr.setName("pages");
		pageAttr.setEType(EcorePackage.Literals.EINT);
		eClass.getEStructuralFeatures().add(pageAttr);

		EAttribute attr = ecoreFactory.createEAttribute();
		attr.setName("readPages");
		attr.setEType(EcorePackage.Literals.EINT);
		eClass.getEStructuralFeatures().add(attr);
		EOperation op = ecoreFactory.createEOperation();
		op.setName("read");
		EParameter p = ecoreFactory.createEParameter();
		p.setEType(EcorePackage.Literals.EINT);
		p.setName("pagesRead");
		op.getEParameters().add(p);
		EAnnotation an = ecoreFactory.createEAnnotation();
		an.setSource("http://www.eclipse.org/emf/2002/GenModel");
		an.getDetails()
				.put("body", "setReadPages(getReadPages() + pagesRead);");
		op.getEAnnotations().add(an);
		eClass.getEOperations().add(op);
		ePackage.getEClassifiers().add(eClass);

		assertNotNull(service);
		EPackage nativePackage = service.supply(ePackage);
		assertNotNull(nativePackage);
		assertEquals(ePackage.getName(), nativePackage.getName());
		assertEquals(ePackage.getNsPrefix(), nativePackage.getNsPrefix());
		assertEquals(ePackage.getNsURI(), nativePackage.getNsURI());
		EClass theClass = (EClass) nativePackage.getEClassifier(eClass
				.getName());
		EObject theObject = nativePackage.getEFactoryInstance()
				.create(theClass);

		int pages = 704;
		pageAttr = (EAttribute) theClass.getEStructuralFeature(pageAttr
				.getName());
		theObject.eSet(pageAttr, pages);
		assertEquals(pages, theObject.eGet(pageAttr));

		int readPagesCount = 9;
		try {
			Method objectMethod = theObject.getClass().getMethod(op.getName(),
					int.class);
			objectMethod.invoke(theObject, readPagesCount);

		} catch (InvocationTargetException e) {
			fail(e.getTargetException().getLocalizedMessage());
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		EStructuralFeature state = theClass.getEStructuralFeature(attr
				.getName());
		assertEquals(readPagesCount, theObject.eGet(state));

		assertEquals(eClass.getName(), theObject.getClass().getSimpleName());

	}

	@Test
	public void query() {
		EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;
		EPackage model = createEPackage("something", "2.0");
		String nsURI = model.getNsURI();
		EClass theClass = ecoreFactory.createEClass();
		theClass.setName("Item");
		EAttribute attr = ecoreFactory.createEAttribute();
		attr.setName("name");
		attr.setEType(EcorePackage.Literals.ESTRING);
		theClass.getEStructuralFeatures().add(attr);
		model.getEClassifiers().add(theClass);
		service.supply(model);
		IEObjectSource source = (IEObjectSource) service
				.getAdapter(IEObjectSource.class);

		SELECT select = new SELECT(new FROM(source), new WHERE(
				new EObjectAttributeValueCondition(
						EcorePackage.Literals.EPACKAGE__NS_URI,
						new StringValue(nsURI))));
		IQueryResult result;
		result = select.execute();
		Iterator<? extends EObject> i = result.getEObjects().iterator();
		while (i.hasNext()) {
			EObject o = i.next();
			if (o instanceof EPackage) {
				EPackage p = (EPackage) o;
				if (p.getNsURI().equals(nsURI)) {
					EClass jClass = (EClass) p.getEClassifier(theClass
							.getName());
					EObject obj = p.getEFactoryInstance().create(jClass);
					assertEquals(theClass.getName(), obj.getClass()
							.getSimpleName());
				}
			}
		}
	}

	@Test
	public void osgiService() {
		BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference<?> serviceReference = bundleContext
				.getServiceReference(ClasSupplier.class.getName());
		ClasSupplier tested = (ClasSupplier) bundleContext
				.getService(serviceReference);
		assertNotNull(tested);
		EPackage ePackage = createEPackage("anything", "0.0");
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		String className0 = "Being";
		eClass.setName(className0);
		String className1 = "Nothing";
		ePackage.getEClassifiers().add(eClass);
		eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(className1);
		ePackage.getEClassifiers().add(eClass);
		EPackage resultPackage = tested.supply(ePackage);
		assertObjectClass(className0, resultPackage);
		assertObjectClass(className1, resultPackage);
	}

	private void assertObjectClass(String className, EPackage resultPackage) {
		EObject result = resultPackage.getEFactoryInstance().create(
				(EClass) resultPackage.getEClassifier(className));
		assertEquals(className, result.getClass().getSimpleName());
	}
}
