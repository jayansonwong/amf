package amf.domain.security

import amf.domain._
import amf.metadata.domain.security.SecuritySchemeModel.{Settings => SettingsField, _}
import org.yaml.model.YPart

case class SecurityScheme(fields: Fields, annotations: Annotations)
    extends DomainElement
    with Linkable
    with WithSettings {
  def name: String                    = fields(Name)
  def `type`: String                  = fields(Type)
  def displayName: String             = fields(DisplayName)
  def description: String             = fields(Description)
  def headers: Seq[Parameter]         = fields(Headers)
  def queryParameters: Seq[Parameter] = fields(QueryParameters)
  def responses: Seq[Response]        = fields(Responses)
  def settings: Settings              = fields(SettingsField)

  def withName(name: String): this.type                               = set(Name, name)
  def withType(`type`: String): this.type                             = set(Type, `type`)
  def withDisplayName(displayName: String): this.type                 = set(DisplayName, displayName)
  def withDescription(description: String): this.type                 = set(Description, description)
  def withHeaders(headers: Seq[Parameter]): this.type                 = setArray(Headers, headers)
  def withQueryParameters(queryParameters: Seq[Parameter]): this.type = setArray(QueryParameters, queryParameters)
  def withResponses(responses: Seq[Response]): this.type              = setArray(Responses, responses)
  def withSettings(settings: Settings): this.type                     = set(SettingsField, settings)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)

  def withHeader(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(Headers, result)
    result
  }

  def withQueryParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(QueryParameters, result)
    result
  }

  def withResponse(name: String): Response = {
    val result = Response().withName(name).withStatusCode(if (name == "default") "200" else name)
    add(Responses, result)
    result
  }

  def withDefaultSettings(): Settings = {
    val settings = Settings()
    set(SettingsField, settings)
    settings
  }

  def withOAuth1Settings(): OAuth1Settings = {
    val settings = OAuth1Settings()
    set(SettingsField, settings)
    settings
  }

  def withOAuth2Settings(): OAuth2Settings = {
    val settings = OAuth2Settings()
    set(SettingsField, settings)
    settings
  }

  def withApiKeySettings(): ApiKeySettings = {
    val settings = ApiKeySettings()
    set(SettingsField, settings)
    settings
  }

  override def linkCopy(): SecurityScheme = SecurityScheme().withId(id)
}

object SecurityScheme {
  def apply(): SecurityScheme = apply(Annotations())

  def apply(ast: YPart): SecurityScheme = apply(Annotations(ast))

  def apply(annotations: Annotations): SecurityScheme = SecurityScheme(Fields(), annotations)
}
