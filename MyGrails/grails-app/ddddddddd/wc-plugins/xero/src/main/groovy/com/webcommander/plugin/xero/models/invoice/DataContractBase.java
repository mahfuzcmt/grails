/*
 *  Copyright 2011 Ross Jourdain
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.webcommander.plugin.xero.models.invoice;

import com.webcommander.plugin.xero.models.ArrayOfValidationError;
import com.webcommander.plugin.xero.models.ArrayOfWarning;

/**
 *
 * @author rossjourdain
 */
public interface DataContractBase {
  ArrayOfValidationError getValidationErrors();
  ArrayOfWarning getWarnings();
}