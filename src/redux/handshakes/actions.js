import { ActionTypes } from './store';
import { getHandshakes, deleteHandshakes } from '../../utils/tokens';
import request from '../../utils/server';

class HandshakeActions {
  getHandshakesAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.GET_HANDSHAKES
      });
      try {
        const response = await getHandshakes();
        dispatch({
          type: ActionTypes.GET_HANDSHAKES_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.GET_HANDSHAKES_FAILED,
          payload: error
        });
      }
    };
  }

  deleteHandshakesAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.DELETE_HANDSHAKES
      });
      try {
        const response = await deleteHandshakes();
        dispatch({
          type: ActionTypes.DELETE_HANDSHAKES_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.DELETE_HANDSHAKES_FAILED,
          payload: error
        });
      }
    };
  }

  sendHandshakesAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.SEND_HANDSHAKES
      });
      try {
        const { handshakes } = await getHandshakes();
        const foundHandshakes = handshakes
          .map(item => item.token?.toUpperCase())
          .filter(item => item !== undefined);
        let response = {};
        if (foundHandshakes.length) {
          response = await request('post', 'check-infection-server', {
            tokens: foundHandshakes,
            num: foundHandshakes.length
          });
        }
        dispatch({
          type: ActionTypes.SEND_HANDSHAKES_SUCCESS,
          payload: response
        });
        return response;
      } catch (error) {
        dispatch({
          type: ActionTypes.SEND_HANDSHAKES_FAILED,
          payload: error
        });
      }
    };
  }

  addHandshakeAction(handshake) {
    return dispatch => {
      dispatch({
        type: ActionTypes.ADD_HANDSHAKE,
        payload: handshake
      });
    };
  }

  changeHandshakeAction(handshake) {
    return dispatch => {
      dispatch({
        type: ActionTypes.CHANGE_HANDHAKE,
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

  saveHandshakeAction() {
    return dispatch => {
      dispatch({
        type: ActionTypes.SAVE_HANDSHAKES
      });
    };
  }

  removeHanshakeAction(time) {
    return dispatch => {
      dispatch({
        type: ActionTypes.REMOVE_HANDSHAKE,
        payload: time
      });
    };
  }
}

const hadshakesActions = new HandshakeActions();
export default hadshakesActions;
