/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Takes care of serializing prism objects and other beans, i.e. converts java form to
 * lexical representation (XML/JSON/YAML strings, DOM tree) or intermediate one (XNode).
 *
 * @author mederly
 */
public interface PrismSerializer<T> {

	/**
	 * Sets the name of the root element. Can be done either here or during call to serialize(..) methods.
	 *
	 * @param elementName Name of the root element
	 * @return Serializer with the root element name set.
	 */
	@NotNull
	PrismSerializer<T> root(QName elementName);

	/**
	 * Sets the context for the serialization operation, containing e.g. serialization options.
	 *
	 * @param context Context to be set.
	 * @return Serializer with the context set.
	 */
	@NotNull
	PrismSerializer<T> context(@Nullable SerializationContext context);

	/**
	 * Sets the serialization options (part of the context).
	 *
	 * @param options Options to be set.
	 * @return Serializer with the options set.
	 */
	@NotNull
	PrismSerializer<T> options(@Nullable SerializationOptions options);

	/**
	 * Serializes given PrismObject.
	 *
	 * @param object PrismObject to be serialized.
	 * @return String representation of the object.
	 */
	@NotNull
	<O extends Objectable> T serialize(@NotNull PrismObject<O> object) throws SchemaException;

	/**
	 * Serializes given prism value (property, reference, or container).
	 * Name of the root element is derived in the following way:
	 * 1. if explicit name is set (
	 * @param value Value to be serialized.
	 * @return String representation of the value.
	 */
	@NotNull
	T serialize(@NotNull PrismValue value) throws SchemaException;

	/**
	 * Serializes given prism value (property, reference, or container).
	 * @param value Value to be serialized.
	 * @param rootName Name of the root element. (Overrides other means of deriving the name.)
	 * @return String representation of the value.
	 */
	@NotNull
	T serialize(@NotNull PrismValue value, @NotNull QName rootName) throws SchemaException;

	@Deprecated
	T serialize(RootXNode xnode) throws SchemaException;

	/**
	 * Serializes an atomic value - i.e. something that fits into a prism property (if such a property would exist).
	 *
	 * value Value to be serialized.
	 * elementName Element name to be used.
	 *
	 * BEWARE, currently works only for values that can be processed via PrismBeanConvertor - i.e. not for special
	 * cases like PolyStringType, ProtectedStringType, etc.
	 */

	T serializeAtomicValue(JAXBElement<?> value) throws SchemaException;
	T serializeAtomicValue(Object value) throws SchemaException;
	T serializeAtomicValue(Object value, QName rootName) throws SchemaException;
	T serializeAnyData(Object value) throws SchemaException;
	T serializeAnyData(Object value, QName rootName) throws SchemaException;
}
