/*
 * Copyright (c) 2011 Yan Pujante
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.linkedin.glu.console.provisioner.services.storage

import org.springframework.validation.Errors

/**
 * @author yan@pongasoft.com */
public class SystemStorageException extends Exception
{
  private static final long serialVersionUID = 1L;

  Errors errors

  SystemStorageException()
  {
  }

  SystemStorageException(String msg)
  {
    super(msg)
  }

  SystemStorageException(Errors errors)
  {
    super(errors?.toString())
    this.errors = errors
  }
}