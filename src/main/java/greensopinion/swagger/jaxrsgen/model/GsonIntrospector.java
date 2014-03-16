/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Tasktop EULA
 * which accompanies this distribution, and is available at
 * http://tasktop.com/legal
 *******************************************************************************/

package greensopinion.swagger.jaxrsgen.model;

import greensopinion.swagger.jaxrsgen.model.ApiModel.Property;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.wordnik.swagger.annotations.ApiModelProperty;

public class GsonIntrospector extends JsonIntrospector {

	private final FieldNamingStrategy fieldNamingStrategy = FieldNamingPolicy.IDENTITY;

	@Override
	public ApiModel createApiModel(Class<?> modelClass) {
		String name = ApiTypes.calculateTypeName(modelClass);
		String description = calculateDescription(modelClass);
		LinkedHashMap<String, Property> properties = Maps.newLinkedHashMap();
		List<String> required = Lists.newArrayList();

		for (Class<?> c = modelClass; c != Object.class; c = c.getSuperclass()) {
			for (Field f : c.getDeclaredFields()) {
				if (!isExcluded(f)) {
					String propertyName = getPropertyName(f);
					properties.put(propertyName, createApiModelProperty(f));
					ApiModelProperty apiModelProperty = f.getAnnotation(ApiModelProperty.class);
					if (apiModelProperty != null && apiModelProperty.required()) {
						required.add(propertyName);
					}
				}
			}
		}

		return new ApiModel(name, description, required, properties);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Property createApiModelProperty(Field f) {
		ApiModelProperty apiModelProperty = f.getAnnotation(ApiModelProperty.class);
		String description = null;
		if (apiModelProperty != null) {
			description = apiModelProperty.value();
		}
		List<String> enumeratedValues = null;
		if (f.getType().isEnum()) {
			enumeratedValues = calculateEnumValues((Class<Enum>) f.getType());
		}
		Map<String, String> arrayItems = null;
		if (f.getType().isArray()) {
			arrayItems = ImmutableMap.of("$ref", ApiTypes.calculateTypeName(f.getType().getComponentType()));
		} else if (Set.class.isAssignableFrom(f.getType()) || List.class.isAssignableFrom(f.getType())) {
			arrayItems = ImmutableMap.of("$ref", ApiTypes.calculateTypeParameterName(f.getGenericType()));
		}
		return new Property(ApiTypes.calculateTypeName(f.getType()), ApiTypes.calculateTypeFormat(f.getType()),
				description, enumeratedValues, arrayItems);
	}

	private <T extends Enum<T>> List<String> calculateEnumValues(Class<T> enumClass) {
		List<String> enumeratedValues = Lists.newArrayList();
		for (T enumConstant : enumClass.getEnumConstants()) {
			String name = enumConstant.name();
			SerializedName annotation;
			try {
				annotation = enumClass.getField(name).getAnnotation(SerializedName.class);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
			if (annotation != null) {
				name = annotation.value();
			}
			enumeratedValues.add(name);
		}
		return enumeratedValues;
	}

	private String getPropertyName(Field f) {
		SerializedName name = f.getAnnotation(SerializedName.class);
		if (name != null) {
			return name.value();
		}
		return fieldNamingStrategy.translateName(f);
	}

	private boolean isExcluded(Field f) {
		int modifiers = f.getModifiers();
		if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
			return true;
		}
		Expose expose = f.getAnnotation(Expose.class);
		if (expose != null && !expose.deserialize() && !expose.serialize()) {
			return true;
		}
		return false;
	}

	private String calculateDescription(Class<?> modelClass) {
		com.wordnik.swagger.annotations.ApiModel apiModel = modelClass.getAnnotation(com.wordnik.swagger.annotations.ApiModel.class);
		return apiModel == null ? null : Strings.emptyToNull(apiModel.value());
	}
}