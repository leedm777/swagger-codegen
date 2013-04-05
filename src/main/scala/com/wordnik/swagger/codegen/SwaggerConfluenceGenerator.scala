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

  val identityPF = new PartialFunction[Any, Any] {
    def apply(v1: Any): Any = v1

    def isDefinedAt(x: Any): Boolean = true
  }

  val mapType: PartialFunction[Any, AnyRef] = {
    // escape [ and ]
    case str: String =>
      str.replaceAll("""[\[\]]""", """\\$0""")
    case Some(str: String) =>
      str.replaceAll("""[\[\]]""", """\\$0""")
  }

  val mapParamField: PartialFunction[Any, AnyRef] = {
    case (name @ ("dataType" | "baseType" | "complexType" | "datatype" | "baseTypeVarName"), t) if mapType.isDefinedAt(t) =>
      name-> mapType(t)
  }

  val mapParam: PartialFunction[Any, AnyRef] = {
    case list: Iterable[_] => list.map(mapParamField orElse identityPF)
  }

  val mapParamList: PartialFunction[Any, AnyRef] = {
    case list: Iterable[_] => list.map(mapParam orElse identityPF)
  }

  val mapPath: PartialFunction[Any, AnyRef] = {
    case str: String =>
      // escape { and }
      str.replaceAll("""[{}]""", """\\$0""")
  }

  override def processApiMap(m: Map[String, AnyRef]): Map[String, AnyRef] =
    m.map {
      case ("headerParams", plist) if mapParamList.isDefinedAt(plist) =>
        "headerParams" -> mapParamList(plist)
      case ("returnType", t) if mapType.isDefinedAt(t) =>
        "returnType" -> mapType(t)
      case ("path", p) if mapPath.isDefinedAt(p) =>
        "path" -> mapPath(p)
      case x =>
        x
    }

  val mapVarField: PartialFunction[Any, (String, AnyRef)] = {
    case (name @ ("dataType" | "baseType" | "complexType" | "datatype" | "baseTypeVarName"), t) if mapType.isDefinedAt(t) =>
      name.asInstanceOf[String] -> mapType(t)
    case (name: String, value: AnyRef) =>
      (name, value)
  }

  val mapVar: PartialFunction[Any, AnyRef] = {
    case map: collection.Map[_, _] => map.map(mapVarField).toMap
  }

  val mapVarList: PartialFunction[AnyRef, AnyRef] = {
    case list: Iterable[_] => list.map(mapVar orElse identityPF)
  }

  override def processModelMap(m: Map[String, AnyRef]): Map[String, AnyRef] = {
    println("old: " + m)
    m.map {
      case ("vars", varList) if mapVarList.isDefinedAt(varList) =>
        "vars" -> mapVarList(varList)
      case x =>
        x
    } tap { r =>
      println("new: " + r)
    }
  }

  /** Don't append Api to resource names */
  override def toApiName(name: String) =
    name.replaceAll("[{}]", "") match {
      case str if !str.isEmpty => str(0).toUpper + str.substring(1)
      case str => "Api"
    }
}
