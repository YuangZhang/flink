/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.types.inference.utils;

import org.apache.flink.annotation.Internal;
import org.apache.flink.table.functions.FunctionDefinition;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.inference.CallContext;
import org.apache.flink.table.types.inference.MutableCallContext;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Helper context that deals with adapted arguments.
 *
 * <p>For example, if an argument needs to be casted to a target type, an expression that was a
 * literal before is not a literal anymore in this call context.
 */
@Internal
public final class AdaptedCallContext implements MutableCallContext {

	private final CallContext originalContext;

	private final List<DataType> expectedArguments;

	private final @Nullable DataType outputDataType;

	public AdaptedCallContext(
			CallContext originalContext,
			List<DataType> castedArguments,
			@Nullable DataType outputDataType) {
		this.originalContext = originalContext;
		this.expectedArguments = new ArrayList<>(castedArguments);
		this.outputDataType = outputDataType;
	}

	@Override
	public List<DataType> getArgumentDataTypes() {
		return expectedArguments;
	}

	@Override
	public FunctionDefinition getFunctionDefinition() {
		return originalContext.getFunctionDefinition();
	}

	@Override
	public boolean isArgumentLiteral(int pos) {
		if (isCasted(pos)) {
			return false;
		}
		return originalContext.isArgumentLiteral(pos);
	}

	@Override
	public boolean isArgumentNull(int pos) {
		// null remains null regardless of casting
		return originalContext.isArgumentNull(pos);
	}

	@Override
	public <T> Optional<T> getArgumentValue(int pos, Class<T> clazz) {
		if (isCasted(pos)) {
			return Optional.empty();
		}
		return originalContext.getArgumentValue(pos, clazz);
	}

	@Override
	public String getName() {
		return originalContext.getName();
	}

	private boolean isCasted(int pos) {
		return !originalContext.getArgumentDataTypes().get(pos).equals(expectedArguments.get(pos));
	}

	@Override
	public void mutateArgumentDataType(int pos, DataType newDataType) {
		expectedArguments.set(pos, newDataType);
	}

	@Override
	public Optional<DataType> getOutputDataType() {
		return Optional.ofNullable(outputDataType);
	}
}
