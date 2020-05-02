import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  screen: {
    backgroundColor: '#fefefe',
    flex: 1
  },
  question: {
    marginTop: 10
  },
  questionBtns: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-evenly'
  },
  tile: {
    borderRadius: 10,
    padding: 10,
    margin: 10,
    backgroundColor: '#f7f8fc',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5
  },
  tileHeader: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  headerContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 10,
    marginHorizontal: 25
  },
  container: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginHorizontal: 25
  },
  value: {
    color: 'darkgreen',
    fontWeight: 'bold'
  },
  error: {
    color: 'red'
  },
  eventContainer: {
    // marginVertical: 10,
    marginHorizontal: 25
  },
  eventType: {
    // fontWeight: 'bold'
  },
  eventMessage: {
    color: 'gray'
  }
});
