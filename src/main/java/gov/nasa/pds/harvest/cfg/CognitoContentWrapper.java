package gov.nasa.pds.harvest.cfg;

import gov.nasa.pds.registry.common.CognitoContent;

class CognitoContentWrapper implements CognitoContent {
  final private CognitoType cognito;
  public CognitoContentWrapper (CognitoType cognito){
    this.cognito = cognito;
  }
  @Override
  public String getClientID() {
    return this.cognito.getValue();
  }
  @Override
  public String getGateway() {
    return this.cognito.getGateway();
  }
  @Override
  public String getIDP() {
    return this.cognito.getIDP();
  }
}
