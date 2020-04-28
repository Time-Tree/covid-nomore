import { ActionTypes, initialState } from './store';

function handshakesReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.ADD_HANDSHAKE: {
      return {
        ...state,
        handshakes: [action.payload, ...state.handshakes]
      };
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
