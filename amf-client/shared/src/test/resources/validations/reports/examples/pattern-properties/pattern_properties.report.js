Model: file://amf-client/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: ['note1'] should be integer
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties.raml#/web-api/end-points/%2Ftest/get/200/application%2Fjson/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(18,0)-(19,65)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties.raml
