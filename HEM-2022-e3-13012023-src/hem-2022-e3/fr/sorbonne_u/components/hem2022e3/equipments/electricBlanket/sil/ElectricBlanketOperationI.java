package fr.sorbonne_u.components.hem2022e3.equipments.electricBlanket.sil;
/**
* @author Liuyi CHEN & Hongyu YAN
* @date 3 janv. 2023
* @Description 
*/
public interface ElectricBlanketOperationI {
	
	default void		switchOnBlanket()	{ /* by default, do nothing. */ }
	
	default void		switchOffBlanket()	{ /* by default, do nothing. */ }
	
	default void		setHighTemperture() { /* by default, do nothing. */ }
	
	default void		setLowTemperture()  { /* by default, do nothing. */ }
	
	default void		doNotHeat()			{ /* by default, do nothing. */ }
}
