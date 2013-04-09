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
import com.wordnik.swagger.model.{SwaggerSerializers, Model}
import org.json4s.Formats
import org.json4s.jackson.Serialization
import scala.collection.mutable.Iterable

object SwaggerConfluenceGenerator extends BasicGenerator {
  implicit def tapIt[T](obj: T) = new {
    def tap(fn: T => Any) = {
      fn(obj)
      obj
    }
  }
  def main(args: Array[String]) {
    generateClient(args)
  }

  implicit val formats = SwaggerSerializers.formats

  override def templateDir = "src/main/resources/swagger-confluence"

  override def destinationDir = "samples/docs/swagger-confluence-docs"

  // template used for apis
  apiTemplateFiles += "api.mustache" -> ".wiki"

  override def apiPackage = None

  override def modelPackage = None

  override def supportingFiles = List(
    ("models.mustache", destinationDir, "models.wiki"))

  override def modelToJson(model: Model)(implicit formats: Formats): String =
    Serialization.writePretty(model)

  override def processResponseClass(responseClass: String): Option[String] =
    for (dataType <- super.processResponseClass(responseClass)) yield {
      dataType.replaceAll("""[\[\]]""", """\\$0""")
    }

  override def processResponseDeclaration(responseClass: String): Option[String] =
    for (dataType <- super.processResponseDeclaration(responseClass)) yield {
      dataType.replaceAll("""[\[\]]""", """\\$0""")
    }

  override def toDeclaredType(dataType: String): String =
    super.toDeclaredType(dataType).replaceAll("""[\[\]]""", """\\$0""")

  override def processPathName(path: String): String =
    super.processPathName(path).replaceAll("""[{}]""", """\\$0""")

  /** Don't append Api to resource names */
  override def toApiName(name: String) =
    name.replaceAll("[{}]", "") match {
      case str if !str.isEmpty => str(0).toUpper + str.substring(1)
      case str => "Api"
    }
}
