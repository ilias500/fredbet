package de.fred4jupiter.fredbet.web.matches;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.fred4jupiter.fredbet.domain.Match;
import de.fred4jupiter.fredbet.security.FredBetPermission;
import de.fred4jupiter.fredbet.service.MatchService;
import de.fred4jupiter.fredbet.web.WebMessageUtil;

@Controller
@RequestMapping("/matchresult")
public class MatchResultController {

	private static final String VIEW_EDIT_MATCHRESULT = "matches/matchresult";

	@Autowired
	private MatchService matchService;

	@Autowired
	private WebMessageUtil messageUtil;

	@PreAuthorize("hasAuthority('" + FredBetPermission.PERM_EDIT_MATCH_RESULT + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ModelAndView edit(@PathVariable("id") Long matchId) {
		Match match = matchService.findMatchById(matchId);
		MatchResultCommand matchResultCommand = toMatchResultCommand(match);
		return new ModelAndView(VIEW_EDIT_MATCHRESULT, "matchResultCommand", matchResultCommand);
	}

	private MatchResultCommand toMatchResultCommand(Match match) {
		MatchResultCommand matchResultCommand = new MatchResultCommand();
		matchResultCommand.setMatchId(match.getId());
		matchResultCommand.setCountryTeamOne(match.getCountryOne());
		matchResultCommand.setCountryTeamTwo(match.getCountryTwo());
		matchResultCommand.setGroupMatch(match.isGroupMatch());

		if (match.hasContriesSet()) {
			matchResultCommand.setTeamNameOne(messageUtil.getCountryName(match.getCountryOne()));
			matchResultCommand.setTeamNameTwo(messageUtil.getCountryName(match.getCountryTwo()));

			matchResultCommand.setShowCountryIcons(true);
		} else {
			matchResultCommand.setTeamNameOne(match.getTeamNameOne());
			matchResultCommand.setTeamNameTwo(match.getTeamNameTwo());
		}
		matchResultCommand.setTeamResultOne(match.getGoalsTeamOne());
		matchResultCommand.setTeamResultTwo(match.getGoalsTeamTwo());
		matchResultCommand.setPenaltyWinnerOne(match.isPenaltyWinnerOne());
		return matchResultCommand;
	}

	@PreAuthorize("hasAuthority('" + FredBetPermission.PERM_EDIT_MATCH_RESULT + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView save(MatchResultCommand matchResultCommand, BindingResult result, RedirectAttributes redirect, ModelMap modelMap) {
		if (result.hasErrors()) {
			return new ModelAndView(VIEW_EDIT_MATCHRESULT, "formErrors", result.getAllErrors());
		}

		if (matchResultCommand.hasInvalidGoals()) {
			messageUtil.addErrorMsg(modelMap, "msg.negative.values.not.allowed");
			return new ModelAndView(VIEW_EDIT_MATCHRESULT, "matchResultCommand", matchResultCommand);
		}

		if (matchResultCommand.isOnlyOneResultSet()) {
			messageUtil.addErrorMsg(modelMap, "msg.input.complete.result");
			return new ModelAndView(VIEW_EDIT_MATCHRESULT, "matchResultCommand", matchResultCommand);
		}

		matchService.save(matchResultCommand);
		return new ModelAndView("redirect:/matches#" + matchResultCommand.getMatchId());
	}

}