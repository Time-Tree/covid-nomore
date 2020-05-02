import { ActionTypes, initialState } from './store';

function settingsReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.ADD_EVENT: {
      const newEvents = [action.payload, ...state.events];
      if (newEvents.length > 100) {
        newEvents.pop();
      }
      return {
        ...state,
        events: newEvents
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
