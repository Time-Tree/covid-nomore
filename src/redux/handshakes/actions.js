import { ActionTypes } from './store';

class HandshakeActions {
  addHandshakeAction(handshake) {
    return dispatch => {
      dispatch({
        type: ActionTypes.ADD_HANDSHAKE,
        payload: handshake
      });
    };
  }

  clearHandshakeAction() {
    return dispatch => {
      dispatch({
        type: ActionTypes.CLEAR_HANSHAKES
      });
    };
  }
}

const hadshakesActions = new HandshakeActions();
export default hadshakesActions;
