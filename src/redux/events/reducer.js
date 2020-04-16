import { ActionTypes, initialState } from './store';

function settingsReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.ADD_EVENT: {
      return {
        ...state,
        events: [...state.events, action.payload]
      };
    }
    case ActionTypes.CLEAR_EVENTS: {
      return {
        ...state,
        events: []
      };
    }
    default:
      return state;
  }
}

export default settingsReducer;
