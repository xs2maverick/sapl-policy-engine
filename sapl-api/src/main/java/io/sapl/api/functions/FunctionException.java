/**
 * Copyright © 2020 Dominic Heutelbeck (dominic@heutelbeck.com)
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
package io.sapl.api.functions;

/**
 * Indicates an error during function context setup or function evaluation.
 */
public class FunctionException extends Exception {

	private static final long serialVersionUID = 1235630787233584513L;

	/**
	 * Create a new FunctionException 
	 */
	public FunctionException() {
		super();
	}

	/**
	 * Create a new FunctionException 
	 * @param message a message
	 */
	public FunctionException(String message) {
		super(message);
	}

	/**
	 * Create a new FunctionException 
	 * @param message a message
	 * @param cause causing Throwable
	 */
	public FunctionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new FunctionException 
	 * @param cause causing Throwable
	 */
	public FunctionException(Throwable cause) {
		super(cause);
	}

}
