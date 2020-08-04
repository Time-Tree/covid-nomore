import { ActionTypes } from './store';
import { getTokens, deleteTokens } from '../../utils/tokens';
import { store } from '../store';
import request from '../../utils/server';

class TokensActions {
  getTokensAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.GET_TOKENS
      });
      try {
        const response = await getTokens();
        dispatch({
          type: ActionTypes.GET_TOKENS_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.GET_TOKENS_FAILED,
          payload: error
        });
      }
    };
  }

  deleteTokensAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.DELETE_TOKENS
      });
      try {
        const response = await deleteTokens();
        dispatch({
          type: ActionTypes.DELETE_TOKENS_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.DELETE_TOKENS_FAILED,
          payload: error
        });
      }
    };
  }

  sendTokensAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.SEND_TOKENS
      });
      try {
        const client = store.getState().settings.clientId;
        const { tokens } = await getTokens();
        const foundTokens = tokens
          .map(item => item.token?.toUpperCase())
          .filter(item => item !== undefined);
        const response = await request('post', 'infected', {
          tokens: foundTokens,
          client
        });
        dispatch({
          type: ActionTypes.SEND_TOKENS_SUCCESS,
          payload: response.data
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.SEND_TOKENS_FAILED,
          payload: error
        });
      }
    };
  }

  addTokenAction(token) {
    return dispatch => {
      dispatch({
        type: ActionTypes.ADD_TOKEN,
        payload: token
      });
    };
  }

  clearTokensAction() {
    return dispatch => {
      dispatch({
        type: ActionTypes.CLEAR_TOKENS
      });
    };
  }

  setSync(payload) {
    return dispatch => {
      dispatch({
        type: ActionTypes.SET_SYNC,
        payload
      });
    };
  }
}

const tokenActions = new TokensActions();
export default tokenActions;
