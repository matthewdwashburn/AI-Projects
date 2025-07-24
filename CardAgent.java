package cardgames;
import java.util.ArrayList;

public class CardAgent {
	/** Pointer to card game*/
	private CardDraw cd;
	
	/** Probability next card has the same value*/
	private double nextEqual;
	
	/** Probability next card has higher value */
	private double nextHigher;
	
	/** Probability next card is of the same suit */
	private double nextSameSuit;

	/** ArrayList of cards seen (from card deck*/
	private ArrayList<Card> cardsSeen;

	/**
	 * Constructor
	 * @param newCd Pointer to card game
	 */
	public CardAgent(CardDraw newCd)
	{
		cd = newCd;
		cardsSeen = cd.getCardsSeen();
	}
	
	/**
	 * Setter for probabilities
	 */
	private void calculateProbabilities()
	{
		setNextEqual();
		setNextHigher();
		setNextSameSuit();
	}
	
	/**
	 * Calculate probability next card has higher value
	 */
	private void setNextHigher()
	{
		int value = cardsSeen.get(cardsSeen.size()-1).getValue();
		int numberLeft = (13-value)*4;
		for (int i = 0; i < cardsSeen.size()-1; i++)
		{
			if (cardsSeen.get(i).getValue() > value)
					numberLeft--;
		}
		nextHigher = (double)numberLeft/(52-cardsSeen.size());
	}
	
	/**
	 * Calculate probability next card is of same suit
	 */
	private void setNextSameSuit()
	{
		int suit = cardsSeen.get(cardsSeen.size()-1).getValue();
		int numberLeft = 12;
		for (int i = 0; i < cardsSeen.size()-1; i++)
		{
			if (cardsSeen.get(i).getValue() == suit)
					numberLeft--;
		}
		nextSameSuit = (double)numberLeft/(52-cardsSeen.size());
	}
	
	/** 
	 * Calculate probability next card has the same value
	 */
	private void setNextEqual()
	{
		int value = cardsSeen.get(cardsSeen.size()-1).getValue();
		int numberLeft = 3;
		for (int i = 0; i < cardsSeen.size()-1; i++)
		{
			if (cardsSeen.get(i).getValue() == value)
					numberLeft--;
		}
		nextEqual = (double)numberLeft/(52-cardsSeen.size());
	}
	
	/**
	 * Tostring method
	 */
	public String toString()
	{
		calculateProbabilities();
		String s = "Probability next card has same value: " + nextEqual + "\n";
		s += "Probability next card has higher value: " + nextHigher + "\n";
		s += "Probability next card is of the same suit: " + nextSameSuit + "\n";
		return s;
	}
}
