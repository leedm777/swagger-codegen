/**
 * Copyright 2012 Wordnik, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.wordnik.swagger.codegen.BasicGenerator
import com.wordnik.swagger.model.{Operation, Model, ApiListing}
import scala.collection.mutable

object SwaggerConfluenceGenerator extends BasicGenerator {
  def main(args: Array[String]) {
    generateClient(args)
  }

  override def templateDir = "src/main/resources/swagger-confluence"

  override def destinationDir = "samples/docs/swagger-confluence-docs"

  // template used for apis
  apiTemplateFiles += "api.mustache" -> ".wiki"

  override def apiPackage = None

  override def supportingFiles = List(
    ("models.mustache", destinationDir, "models.wiki"))

  override def extractApiOperations(apiListings: List[ApiListing], allModels: mutable.HashMap[String, Model])(implicit basePath: String): List[(String, String, Operation)] = {
    // escape { and } in apiPath
    for ((base, apiPath, op) <- super.extractApiOperations(apiListings, allModels)) yield {
      (base, apiPath.replaceAll("[{}]", "\\\\$0"), op)
    }
  }

  /** Don't append Api to resource names */
  override def toApiName(name: String) =
    name.replaceAll("[{}]", "") match {
      case str if !str.isEmpty => str(0).toUpper + str.substring(1)
      case str => "Api"
    }
}
