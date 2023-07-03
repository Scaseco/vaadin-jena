package org.aksw.facete.v4.impl;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;

import com.google.common.collect.Iterables;


/**
 * Accumulator for elements. Elements are added to an ElementGroup that acts as a container
 * whereas the resulting Element may be a different element, such as an ElementOptional.
 */
public class ElementAcc {
    protected Var rootVar; // The root variable of the element to which any child elements connect

    protected Element element;

    /** Function to build an element from the local elements and the elements of the children */
    protected BiFunction<Element, List<Element>, Element> resultFactory;
    // protected ElementGroup container;

    // protected ElementAccTree tree;

//    public static ElementAcc newRoot() {
//        return new ElementAcc(Var.alloc("root"), new ElementGroup(), ElementAcc::collectIntoGroup);
//    }

    public ElementAcc(Var rootVar, Element element, BiFunction<Element, List<Element>, Element> resultFactory) {
        super();
        this.rootVar = rootVar;
        this.element = element;
        this.resultFactory = resultFactory;
    }

    public static Element collectIntoGroup(Element parentElts, List<Element> childElts) {
        Element result = ElementUtils.flatMerge(
                Iterables.concat(Collections.singleton(parentElts), childElts));

//    	ElementGroup group = new ElementGroup();
//        ElementUtils.copyElements(group, parentElts);
//        for (Element childElt : childElts) {
//            ElementUtils.copyElements(group, childElt);
//        }
//        Element result = ElementUtils.flatten(group);
        return result;
    }

    public static Element collectIntoOptionalGroup(Element parentElts, List<Element> childElts) {
        Element elt = collectIntoGroup(parentElts, childElts);
        return new ElementOptional(elt);
    }

    public Element getElement() {
        return element;
    }

    public BiFunction<Element, List<Element>, Element> getFactory() {
        return resultFactory;
    }


//    public Element getResultElement() {
//        List<Element> childElts = children.values().stream().map(ElementAcc::getResultElement).collect(Collectors.toList());
//        Element result = resultFactory.apply(localElement, childElts);
//        return result;
//    }

    /*
     * public ElementGroup getContainer() { return container; }
     *
     * @Override public String toString() { Element currentElt = getResultElement();
     * return "ElementAcc [rootVar=" + rootVar + ", resultElement=" + currentElt +
     * "]"; }
     */}
