import update from 'immutability-helper';
import { ActionTypes, initialState } from './store';

function handshakesReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.GET_HANDSHAKES: {
      return {
        ...state,
        get_handshakes_pending: true
      };
    }
    case ActionTypes.GET_HANDSHAKES_SUCCESS: {
      return {
        ...state,
        sync: action.payload.sync,
        handshakes: action.payload.handshakes,
        get_handshakes_pending: false
      };
    }
    case ActionTypes.GET_HANDSHAKES_FAILED: {
      return {
        ...state,
        get_handshakes_error: action.payload,
        get_handshakes_pending: false
      };
    }
    case ActionTypes.DELETE_HANDSHAKES: {
      return {
        ...state,
        delete_handshakes_pending: true
      };
    }
    case ActionTypes.DELETE_HANDSHAKES_SUCCESS: {
      return {
        ...state,
        handshakes: [],
        delete_handshakes_pending: false
      };
    }
    case ActionTypes.DELETE_HANDSHAKES_FAILED: {
      return {
        ...state,
        delete_handshakes_error: action.payload,
        delete_handshakes_pending: false
      };
    }
    case ActionTypes.SEND_HANDSHAKES: {
      return {
        ...state,
        send_handshakes_pending: true
      };
    }
    case ActionTypes.SEND_HANDSHAKES_SUCCESS: {
      return {
        ...state,
        send_handshakes_pending: false
      };
    }
    case ActionTypes.SEND_HANDSHAKES_FAILED: {
      return {
        ...state,
        send_handshakes_error: action.payload,
        send_handshakes_pending: false
      };
    }
    case ActionTypes.ADD_HANDSHAKE: {
      return {
        ...state,
        handshakes: [...action.payload, ...state.handshakes]
      };
    }
    case ActionTypes.CHANGE_HANDHAKE: {
      const { payload } = action;
      const { handshakes } = state;
      const index = handshakes.findIndex(h => h.time === payload.time);
      if (index >= 0) {
        const newHandshake = update(handshakes[index], { $set: payload });
        return update(state, {
          handshakes: { $splice: [[index, 1, newHandshake]] }
        });
      }
      return state;
    }
    case ActionTypes.REMOVE_HANDSHAKE: {
      const index = state.handshakes.findIndex(h => h.time === action.payload);
      if (index >= 0) {
        return update(state, { handshakes: { $splice: [[index, 1]] } });
      }
      return state;
    }
    case ActionTypes.CLEAR_HANSHAKES: {
      return {
        ...state,
        handshakes: []
      };
    }
    case ActionTypes.SAVE_HANDSHAKES: {
      return {
        ...state,
        lastUpdated: new Date().getTime()
      };
    }
    default:
      return state;
  }
}

export default handshakesReducer;
