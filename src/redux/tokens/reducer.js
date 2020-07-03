import { ActionTypes, initialState } from './store';

function settingsReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.ADD_TOKEN: {
      const newTokens = [...action.payload, ...state.tokens].slice(0, 100);
      return {
        ...state,
        tokens: newTokens
      };
    }
    case ActionTypes.CLEAR_TOKENS: {
      return {
        ...state,
        tokens: []
      };
    }
    case ActionTypes.SET_SYNC: {
      return {
        ...state,
        sync: action.payload
      };
    }
    case ActionTypes.GET_TOKENS: {
      return {
        ...state,
        get_tokens_pending: true
      };
    }
    case ActionTypes.GET_TOKENS_SUCCESS: {
      return {
        ...state,
        sync: action.payload.sync,
        tokens: action.payload.tokens,
        get_tokens_pending: false
      };
    }
    case ActionTypes.GET_TOKENS_FAILED: {
      return {
        ...state,
        get_tokens_error: action.payload,
        get_tokens_pending: false
      };
    }
    case ActionTypes.DELETE_TOKENS: {
      return {
        ...state,
        delete_tokens_pending: true
      };
    }
    case ActionTypes.DELETE_TOKENS_SUCCESS: {
      return {
        ...state,
        tokens: [],
        delete_tokens_pending: false
      };
    }
    case ActionTypes.DELETE_TOKENS_FAILED: {
      return {
        ...state,
        delete_tokens_error: action.payload,
        delete_tokens_pending: false
      };
    }
    case ActionTypes.SEND_TOKENS: {
      return {
        ...state,
        send_tokens_pending: true
      };
    }
    case ActionTypes.SEND_TOKENS_SUCCESS: {
      return {
        ...state,
        send_tokens_pending: false
      };
    }
    case ActionTypes.SEND_TOKENS_FAILED: {
      return {
        ...state,
        send_tokens_error: action.payload,
        send_tokens_pending: false
      };
    }
    default:
      return state;
  }
}

export default settingsReducer;
