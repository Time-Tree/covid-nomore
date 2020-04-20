import { ActionTypes, initialState } from './store';

function handshakesReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.ADD_HANDSHAKE: {
      return {
        ...state,
        handshakes: [...state.handshakes, action.payload]
      };
    }
    case ActionTypes.CLEAR_HANSHAKES: {
      return {
        ...state,
        handshakes: []
      };
    }
    default:
      return state;
  }
}

export default handshakesReducer;
