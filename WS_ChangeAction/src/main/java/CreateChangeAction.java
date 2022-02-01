public class CreateChangeAction {
	
	/**
	 * This method calls the json file named ChangeAction_create.json in order to create the Change Action with the following attributes.
	 * <p>
	 * <b>Current list of attributes:</b>
	 * <ol>
	 * <li>policy</li>
	 * <li>description</li>
	 * <li>title</li>
	 * <li>name</li>
	 * <li>severity</li>
	 * <li>Estimated Start Date</li>
	 * <li>Estimated Completion Date</li>
	 * <li>xmlApplicability</li>
	 * </ol>
	 * If you have other attributes you need to fill at the creation of the Change Action, you need to add them in the ChangeAction_create.json
	 */

	public static void main(String[] args) {
		try {		
			//ChangeActionManager cam = new ChangeActionManager("user00", "ctx::VPLMProjectLeader.Company Name.Common Space");
			ChangeActionManager cam = new ChangeActionManager(Util.USER, Util.CONTEXT, Util.inputfile, Util.logFile);
			cam.CreateChangeAction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
