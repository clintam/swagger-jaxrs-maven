/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies.
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
 *******************************************************************************/

package greensopinion.swagger.jaxrsgen.mock.noswagger.model;

import java.net.URI;
import java.util.Date;

public class Pet extends PetValues {

	private long id;

	private URI url;

	private Date created;

	public long getId() {
		return id;
	}

	public Date getCreated() {
		return created;
	}

	public URI getUrl() {
		return url;
	}

}
